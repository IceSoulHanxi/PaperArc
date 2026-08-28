package com.ixnah.mc.paperarc.mixin.common.entity;

import com.destroystokyo.paper.event.entity.EntityTeleportEndGatewayEvent;
import com.llamalad7.mixinextras.sugar.Local;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftEndGateway;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's EntityTeleportEndGatewayEvent
 * (Implement-EntityTeleportEndGatewayEvent.patch), 1.20.1 form.
 *
 * <p>1.20.1 fires the event inside the static
 * {@code TheEndGatewayBlockEntity#teleportEntity(Level, BlockPos, BlockState, Entity, TheEndGatewayBlockEntity)}
 * right before {@code entity.setPortalCooldown()}. Cancelling skips the whole
 * teleport (setPortalCooldown + teleportToWithTicket). The exit position is
 * the local {@code BlockPos blockposition1} computed from the exit portal.
 */
@Mixin(TheEndGatewayBlockEntity.class)
public abstract class EntityTeleportEndGatewayMixin {

    @Inject(
            method = "teleportEntity(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/block/entity/TheEndGatewayBlockEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;setPortalCooldown()V"
            ),
            cancellable = true
    )
    private static void paperarc$fireGatewayTeleportEvent(Level level, BlockPos pos,
                                                          net.minecraft.world.level.block.state.BlockState state,
                                                          Entity entity1, TheEndGatewayBlockEntity blockEntity,
                                                          CallbackInfo ci,
                                                          @Local(ordinal = 0) ServerLevel worldserver,
                                                          @Local(ordinal = 0) BlockPos blockposition1) {
        Location location = new Location(PaperArcBridge.bukkitWorld(worldserver), blockposition1.getX() + 0.5D,
                blockposition1.getY(), blockposition1.getZ() + 0.5D);
        location.setPitch(entity1.getXRot());
        org.bukkit.entity.Entity bukkitEntity = PaperArcBridge.bukkitEntity(entity1);
        location.setYaw(bukkitEntity.getLocation().getYaw());

        EntityTeleportEndGatewayEvent event = new EntityTeleportEndGatewayEvent(
                bukkitEntity, bukkitEntity.getLocation(), location,
                new CraftEndGateway(PaperArcBridge.bukkitWorld(worldserver), blockEntity));
        if (!event.callEvent()) {
            ci.cancel();
        }
    }
}
