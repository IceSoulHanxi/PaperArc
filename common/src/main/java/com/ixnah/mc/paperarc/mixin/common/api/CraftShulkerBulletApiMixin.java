package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftShulkerBullet;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Adds Paper's More-Projectile-API shulker-bullet methods
 * (patches/server/More-Projectile-API.patch -> CraftShulkerBullet).
 *
 * Vanilla mojmap {@link ShulkerBullet} already stores all state as private
 * fields ({@code currentMoveDirection}, {@code flightSteps},
 * {@code targetDeltaX/Y/Z}); Paper's NMS-side patch only widens access, so a
 * Craft-host mixin reads/writes them reflectively.
 */
@Mixin(CraftShulkerBullet.class)
public abstract class CraftShulkerBulletApiMixin {

    @Shadow
    public abstract ShulkerBullet getHandle();

    @Unique
    private static volatile Map<String, Field> PAPERARC$FIELDS;

    @Unique
    private static Field paperarc$field(String name) throws ReflectiveOperationException {
        Map<String, Field> cache = PAPERARC$FIELDS;
        if (cache != null) {
            Field f = cache.get(name);
            if (f != null) {
                return f;
            }
        }
        synchronized (CraftShulkerBulletApiMixin.class) {
            if (PAPERARC$FIELDS == null) {
                PAPERARC$FIELDS = new HashMap<>();
            }
            Field resolved = ShulkerBullet.class.getDeclaredField(name);
            resolved.setAccessible(true);
            PAPERARC$FIELDS.put(name, resolved);
            return resolved;
        }
    }

    @Unique
    private double paperarc$targetDelta(String axis) {
        try {
            return paperarc$field(axis).getDouble(getHandle());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS ShulkerBullet field " + axis + " not accessible", e);
        }
    }

    @Unique
    public Vector getTargetDelta() {
        return new Vector(
                paperarc$targetDelta("targetDeltaX"),
                paperarc$targetDelta("targetDeltaY"),
                paperarc$targetDelta("targetDeltaZ"));
    }

    @Unique
    public void setTargetDelta(Vector vector) {
        ShulkerBullet handle = getHandle();
        try {
            paperarc$field("targetDeltaX").setDouble(handle, vector.getX());
            paperarc$field("targetDeltaY").setDouble(handle, vector.getY());
            paperarc$field("targetDeltaZ").setDouble(handle, vector.getZ());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to write NMS ShulkerBullet target delta", e);
        }
    }

    @Unique
    public BlockFace getCurrentMovementDirection() {
        try {
            Direction dir = (Direction) paperarc$field("currentMoveDirection").get(getHandle());
            if (dir == null) {
                return null; // random direction
            }
            return CraftBlock.notchToBlockFace(dir);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS ShulkerBullet.currentMoveDirection not accessible", e);
        }
    }

    @Unique
    public void setCurrentMovementDirection(BlockFace blockFace) {
        try {
            Direction dir = blockFace == null ? null : CraftBlock.blockFaceToNotch(blockFace);
            paperarc$field("currentMoveDirection").set(getHandle(), dir);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to write NMS ShulkerBullet.currentMoveDirection", e);
        }
    }

    @Unique
    public int getFlightSteps() {
        try {
            return paperarc$field("flightSteps").getInt(getHandle());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS ShulkerBullet.flightSteps not accessible", e);
        }
    }

    @Unique
    public void setFlightSteps(int flightSteps) {
        try {
            paperarc$field("flightSteps").setInt(getHandle(), flightSteps);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to write NMS ShulkerBullet.flightSteps", e);
        }
    }
}
