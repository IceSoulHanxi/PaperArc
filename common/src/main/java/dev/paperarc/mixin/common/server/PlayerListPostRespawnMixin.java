package dev.paperarc.mixin.common.server;

import net.minecraft.server.players.PlayerList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.paperarc.bridge.PaperArcBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.util.CraftLocation;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Port of Paper's Add-PlayerPostRespawnEvent patch.
 * <p>
 * Host: {@code PlayerList#respawn(ServerPlayer, boolean, RemovalReason)} (the
 * vanilla/CB 3-arg overload; full signature is used in every target so
 * Arclight's added 4/5-arg overloads are never matched).
 * <p>
 * Conflict avoidance vs Arclight's PlayerListMixin:
 * <ul>
 *   <li>it @Decorate's the same findRespawnPositionAndUseSpawnBlock call — we use
 *       a different handler type (@WrapOperation), allowed by conventions;</li>
 *   <li>it @Inject'es at HEAD and RETURN of respawn — our fire point anchors on the
 *       sendAllPlayerInfo INVOKE instead of RETURN.</li>
 * </ul>
 * Deviation: Paper fires the event after the disconnected-save check at the very
 * end; we fire right after sendAllPlayerInfo (slightly earlier, but all teleport
 * and bed-spawn state is already final). The event only fires for natural respawns
 * (no CraftBukkit-forced location), matching Paper's isRespawn flag.
 */
@Mixin(PlayerList.class)
public abstract class PlayerListPostRespawnMixin {

    @Unique
    private static final ThreadLocal<State> paperarc$state = new ThreadLocal<>();

    @Unique
    private static final class State {
        boolean respawn;
        boolean bedSpawn;
        Location location;
        DimensionTransition transition;
    }

    @WrapOperation(
        method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/DimensionTransition$PostDimensionTransition;)Lnet/minecraft/world/level/portal/DimensionTransition;"
        )
    )
    private DimensionTransition paperarc$recordRespawn(ServerPlayer player, boolean flag,
                                                       DimensionTransition.PostDimensionTransition postTransition,
                                                       Operation<DimensionTransition> original) {
        paperarc$state.remove();
        DimensionTransition transition = original.call(player, flag, postTransition);
        if (transition != null) {
            State state = new State();
            state.respawn = true;
            state.transition = transition;
            Vec3 pos = transition.pos();
            state.location = CraftLocation.toBukkit(
                pos,
                PaperArcBridge.bukkitWorld(transition.newLevel()),
                transition.yRot(),
                transition.xRot()
            );
            paperarc$state.set(state);
        }
        return transition;
    }

    @Inject(
        method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;",
        at = @At(
            value = "INVOKE",
            shift = At.Shift.AFTER,
            target = "Lnet/minecraft/server/players/PlayerList;sendAllPlayerInfo(Lnet/minecraft/server/level/ServerPlayer;)V"
        )
    )
    private void paperarc$onPostRespawn(ServerPlayer player, boolean flag, Entity.RemovalReason reason,
                                        CallbackInfoReturnable<ServerPlayer> cir) {
        State state = paperarc$state.get();
        paperarc$state.remove();
        if (state == null || !state.respawn) {
            return;
        }
        // replicate Paper's bed-spawn detection (blockstate read is stable by now)
        BlockPos blockPos = BlockPos.containing(state.transition.pos());
        if (state.transition.newLevel().getBlockState(blockPos).is(BlockTags.BEDS)
            && !state.transition.missingRespawnBlock()) {
            state.bedSpawn = true;
        }
        ServerPlayer respawned = cir.getReturnValue();
        if (respawned == null) {
            return;
        }
        PlayerPostRespawnEvent event = new PlayerPostRespawnEvent(
            PaperArcBridge.bukkitPlayer(respawned),
            state.location,
            state.bedSpawn
        );
        PaperArcBridge.fire(event);
    }
}
