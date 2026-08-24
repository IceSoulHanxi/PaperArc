package com.ixnah.mc.paperarc.mixin.common.server;

import net.minecraft.server.players.PlayerList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import com.ixnah.mc.paperarc.bridge.RespawnCapture;
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
 * Host: Arclight REPLACES the respawn flow at runtime with its CB-style
 * re-implementation {@code PlayerListMixin#respawn(ServerPlayer, boolean,
 * RemovalReason, PlayerRespawnEvent.RespawnReason, Location)} (plain merged
 * overload; the vanilla 3-arg body never executes), so both anchors target
 * that 5-arg handler:
 * <ul>
 *   <li>WrapOperation around its findRespawnPositionAndUseSpawnBlock INVOKE
 *       records natural-respawn state (only runs when location == null);</li>
 *   <li>Inject AFTER its sendAllPlayerInfo INVOKE fires the event once all
 *       teleport/bed-spawn state is final — matching Paper's placement.</li>
 * </ul>
 * require = 0 keeps boot safe if Arclight refactors the handler.
 */
@Mixin(PlayerList.class)
public abstract class PlayerListPostRespawnMixin {

    @Unique
    private static final ThreadLocal<RespawnCapture> paperarc$state = new ThreadLocal<>();

    private static final String PAPERARC$RESPAWN =
            "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;"
            + "Lorg/bukkit/event/player/PlayerRespawnEvent$RespawnReason;Lorg/bukkit/Location;)"
            + "Lnet/minecraft/server/level/ServerPlayer;";

    @WrapOperation(
        method = PAPERARC$RESPAWN,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/DimensionTransition$PostDimensionTransition;)Lnet/minecraft/world/level/portal/DimensionTransition;",
            remap = false
        ),
        require = 0
    )
    private DimensionTransition paperarc$recordRespawn(ServerPlayer player, boolean flag,
                                                       DimensionTransition.PostDimensionTransition postTransition,
                                                       Operation<DimensionTransition> original) {
        paperarc$state.remove();
        DimensionTransition transition = original.call(player, flag, postTransition);
        if (transition != null) {
            RespawnCapture state = new RespawnCapture();
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
        method = PAPERARC$RESPAWN,
        at = @At(
            value = "INVOKE",
            shift = At.Shift.AFTER,
            target = "Lnet/minecraft/server/players/PlayerList;sendAllPlayerInfo(Lnet/minecraft/server/level/ServerPlayer;)V",
            remap = false
        ),
        require = 0
    )
    private void paperarc$onPostRespawn(ServerPlayer player, boolean flag, Entity.RemovalReason reason,
                                        org.bukkit.event.player.PlayerRespawnEvent.RespawnReason respawnReason,
                                        Location bukkitLocation,
                                        CallbackInfoReturnable<ServerPlayer> cir) {
        RespawnCapture state = paperarc$state.get();
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
