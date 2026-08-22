package dev.paperarc.mixin.common.player;

import com.mojang.datafixers.util.Either;
import dev.paperarc.bridge.PaperArcBridge;
import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Port of Paper's Add-PlayerSetSpawnEvent patch.
 *
 * Injects at HEAD of the vanilla five-arg
 * {@code ServerPlayer#setRespawnPosition(ResourceKey, BlockPos, float, boolean,
 * boolean)}: fires {@link PlayerSetSpawnEvent}, supports plugin-side
 * modification of location/forced/notification by re-implementing the vanilla
 * tail (respawn field writes + system message) and cancelling the original
 * body.
 *
 * The CraftBukkit-compat {@code PlayerSpawnChangeEvent} is intentionally NOT
 * fired here — Arclight's {@code @Decorate(method = "setRespawnPosition")}
 * already fires it around this method, giving Paper's ordering (compat event
 * first, then PlayerSetSpawnEvent).
 *
 * Cause resolution: vanilla's method carries no cause parameter, so caller
 * mixins mark a ThreadLocal ({@link #paperarc$pushSpawnCause}) around known
 * call sites — SetSpawnCommand (COMMAND), startSleepInBed (BED), respawn anchor
 * (RESPAWN_ANCHOR). Unmarked paths (CraftPlayer plugin calls, respawn reset)
 * fall back to UNKNOWN.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerSetSpawnMixin {

    @Shadow
    private BlockPos respawnPosition;

    @Shadow
    private ResourceKey<Level> respawnDimension;

    @Shadow
    private float respawnAngle;

    @Shadow
    private boolean respawnForced;

    @Shadow
    public abstract void sendSystemMessage(net.minecraft.network.chat.Component message);

    @Unique
    private static final ThreadLocal<PlayerSetSpawnEvent.Cause> paperarc$cause = new ThreadLocal<>();

    /** Caller mixins mark the Paper spawn-cause before invoking setRespawnPosition. */
    @Unique
    public static void paperarc$pushSpawnCause(PlayerSetSpawnEvent.Cause cause) {
        paperarc$cause.set(cause);
    }

    /** Clears any stale cause marker; also used defensively at caller exits. */
    @Unique
    public static void paperarc$clearSpawnCause() {
        paperarc$cause.remove();
    }

    @Inject(
        method = "setRespawnPosition(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/BlockPos;FZZ)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void paperarc$fireSetSpawn(ResourceKey<Level> dimension, BlockPos pos, float angle,
                                       boolean forced, boolean sendMessage, CallbackInfo ci) {
        PlayerSetSpawnEvent.Cause cause = paperarc$cause.get();
        paperarc$cause.remove();

        ServerPlayer self = (ServerPlayer) (Object) this;
        MinecraftServer server = self.server;
        if (server == null || dimension == null || server.getLevel(dimension) == null) {
            return; // unloaded dimension: keep vanilla behaviour
        }
        ServerLevel newLevel = server.getLevel(dimension);

        Location spawnLoc = null;
        boolean willNotify = false;
        if (pos != null) {
            boolean same = pos.equals(this.respawnPosition) && dimension.equals(this.respawnDimension);
            spawnLoc = new Location(PaperArcBridge.bukkitWorld(newLevel),
                pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, angle, 0.0F);
            willNotify = sendMessage && !same;
        }

        PlayerSetSpawnEvent event = new PlayerSetSpawnEvent(
            PaperArcBridge.bukkitPlayer(self),
            cause,
            spawnLoc,
            forced,
            willNotify,
            willNotify ? Component.translatable("block.minecraft.set_spawn") : null
        );
        if (!event.callEvent()) {
            ci.cancel();
            return;
        }

        Location newLoc = event.getLocation();
        if (newLoc != null && newLoc.getWorld() != null) {
            ResourceKey<Level> newDim = ((CraftWorld) newLoc.getWorld()).getHandle().dimension();
            BlockPos newPos = BlockPos.containing(newLoc.getX(), newLoc.getY(), newLoc.getZ());
            float newAngle = newLoc.getYaw();
            boolean newForced = event.isForced();

            if (event.willNotifyPlayer()) {
                this.sendSystemMessage(paperarc$adventureToVanilla(event.getNotification()));
            }
            this.respawnPosition = newPos;
            this.respawnDimension = newDim;
            this.respawnAngle = newAngle;
            this.respawnForced = newForced;
        } else {
            this.respawnPosition = null;
            this.respawnDimension = Level.OVERWORLD;
            this.respawnAngle = 0.0F;
            this.respawnForced = false;
        }
        ci.cancel(); // state applied from the event above; skip the vanilla body
    }

    /**
     * BED cause marker: startSleepInBed sets the player's respawn to the bed.
     * Cleared at RETURN so an early-out (obstructed bed etc.) cannot leak BED
     * into an unrelated later setRespawnPosition call.
     */
    @Inject(method = "startSleepInBed", at = @At("HEAD"))
    private void paperarc$sleepPushCause(BlockPos pos,
                                         CallbackInfoReturnable<Either<BedSleepingProblem, net.minecraft.util.Unit>> cir) {
        paperarc$cause.set(PlayerSetSpawnEvent.Cause.BED);
    }

    @Inject(method = "startSleepInBed", at = @At("RETURN"))
    private void paperarc$sleepPopCause(BlockPos pos,
                                        CallbackInfoReturnable<Either<BedSleepingProblem, net.minecraft.util.Unit>> cir) {
        paperarc$cause.remove();
    }

    /**
     * Minimal adventure -> vanilla conversion (no PaperAdventure on this
     * toolchain): translatable keeps its key, plain text becomes a literal,
     * anything else falls back to the vanilla "block.minecraft.set_spawn"
     * message.
     */
    @Unique
    private static net.minecraft.network.chat.Component paperarc$adventureToVanilla(Component notification) {
        if (notification instanceof TranslatableComponent translatable) {
            return net.minecraft.network.chat.Component.translatable(translatable.key());
        }
        if (notification instanceof TextComponent text) {
            return net.minecraft.network.chat.Component.literal(text.content());
        }
        return net.minecraft.network.chat.Component.translatable("block.minecraft.set_spawn");
    }
}
