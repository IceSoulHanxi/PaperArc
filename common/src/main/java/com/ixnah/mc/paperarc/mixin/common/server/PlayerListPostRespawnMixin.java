package com.ixnah.mc.paperarc.mixin.common.server;

import net.minecraft.server.players.PlayerList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's PlayerPostRespawnEvent (Implement-PlayerPostRespawnEvent.patch),
 * 1.20.1 Arclight form.
 *
 * <p>Arclight re-implements the respawn flow in its own
 * {@code PlayerList#respawn(ServerPlayer, ServerLevel, boolean, Location, boolean, PlayerRespawnEvent.RespawnReason)}
 * handler. Paper fires {@link PlayerPostRespawnEvent} after the respawn is
 * finalised, only for natural respawns ({@code location == null} at entry).
 * We record that flag at HEAD and fire the event after the final
 * {@code sendAllPlayerInfo} invocation, once all teleport/bed state is set.
 *
 * <p>Known deviation: {@code isBedSpawn} is not reconstructed (the flag is a
 * method-local in the 6-arg handler that is not always defined); it is passed
 * as {@code false}. Event firing and cancel-agnostic semantics match Paper.
 */
@Mixin(PlayerList.class)
public abstract class PlayerListPostRespawnMixin {

    @Unique
    private static final ThreadLocal<Boolean> paperarc$isRespawn = new ThreadLocal<>();

    @Inject(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/level/ServerLevel;ZLorg/bukkit/Location;ZLorg/bukkit/event/player/PlayerRespawnEvent$RespawnReason;)Lnet/minecraft/server/level/ServerPlayer;",
            at = @At("HEAD"),
            remap = false)
    private void paperarc$recordNaturalRespawn(ServerPlayer playerIn, net.minecraft.server.level.ServerLevel worldIn,
                                               boolean flag, Location location, boolean avoidSuffocation,
                                               org.bukkit.event.player.PlayerRespawnEvent.RespawnReason respawnReason,
                                               CallbackInfo ci) {
        paperarc$isRespawn.set(location == null);
    }

    @WrapOperation(
        method = "respawn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/level/ServerLevel;ZLorg/bukkit/Location;ZLorg/bukkit/event/player/PlayerRespawnEvent$RespawnReason;)Lnet/minecraft/server/level/ServerPlayer;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/players/PlayerList;sendAllPlayerInfo(Lnet/minecraft/server/level/ServerPlayer;)V",
            remap = false
        ),
        remap = false
    )
    private void paperarc$onPostRespawn(PlayerList self, ServerPlayer playerIn, Operation<Void> original) {
        original.call(self, playerIn);
        Boolean isRespawn = paperarc$isRespawn.get();
        paperarc$isRespawn.remove();
        if (Boolean.TRUE.equals(isRespawn)) {
            org.bukkit.entity.Player bukkit = PaperArcBridge.bukkitPlayer(playerIn);
            PlayerPostRespawnEvent event = new PlayerPostRespawnEvent(
                bukkit, bukkit.getLocation(), false);
            PaperArcBridge.fire(event);
        }
    }
}
