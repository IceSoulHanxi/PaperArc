package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.entity.CraftShulkerBullet;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;


/**
 * Adds Paper's More-Projectile-API shulker-bullet methods
 * (patches/server/More-Projectile-API.patch -> CraftShulkerBullet).
 *
 * Vanilla mojmap {@link ShulkerBullet} already stores all state as private
 * fields ({@code currentMoveDirection}, {@code flightSteps},
 * {@code targetDeltaX/Y/Z}); Paper's NMS-side patch only widens access, and
 * {@code paperarc.accesswidener} does the same.
 */
@Mixin(CraftShulkerBullet.class)
public abstract class CraftShulkerBulletApiMixin {

    @Shadow
    public abstract ShulkerBullet getHandle();

    @Unique
    public Vector getTargetDelta() {
        ShulkerBullet handle = getHandle();
        return new Vector(handle.targetDeltaX, handle.targetDeltaY, handle.targetDeltaZ);
    }

    @Unique
    public void setTargetDelta(Vector vector) {
        ShulkerBullet handle = getHandle();
        handle.targetDeltaX = vector.getX();
        handle.targetDeltaY = vector.getY();
        handle.targetDeltaZ = vector.getZ();
    }

    @Unique
    public BlockFace getCurrentMovementDirection() {
        Direction dir = getHandle().currentMoveDirection;
        if (dir == null) {
            return null; // random direction
        }
        return CraftBlock.notchToBlockFace(dir);
    }

    @Unique
    public void setCurrentMovementDirection(BlockFace blockFace) {
        getHandle().currentMoveDirection = blockFace == null ? null : CraftBlock.blockFaceToNotch(blockFace);
    }

    @Unique
    public int getFlightSteps() {
        return getHandle().flightSteps;
    }

    @Unique
    public void setFlightSteps(int flightSteps) {
        getHandle().flightSteps = flightSteps;
    }
}
