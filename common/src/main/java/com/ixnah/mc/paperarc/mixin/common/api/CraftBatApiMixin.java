package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ambient.Bat;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftBat;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Bat target-location API.
 *
 * Paper publicizes the private NMS field {@code Bat.targetPosition} via an access
 * transformer; a Craft-host mixin cannot shadow NMS privates, so the field is
 * accessed reflectively (mojmap runtime name: targetPosition).
 */
@Mixin(CraftBat.class)
public abstract class CraftBatApiMixin {

    @Shadow
    public abstract Bat getHandle();

    @Unique
    private static volatile java.lang.reflect.Field PAPERARC$TARGET_POSITION_FIELD;

    @Unique
    private static java.lang.reflect.Field paperarc$targetPositionField() {
        java.lang.reflect.Field f = PAPERARC$TARGET_POSITION_FIELD;
        if (f == null) {
            synchronized (CraftBatApiMixin.class) {
                if (PAPERARC$TARGET_POSITION_FIELD == null) {
                    try {
                        java.lang.reflect.Field resolved = Bat.class.getDeclaredField("targetPosition");
                        resolved.setAccessible(true);
                        PAPERARC$TARGET_POSITION_FIELD = resolved;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("NMS Bat.targetPosition field not found", e);
                    }
                }
                f = PAPERARC$TARGET_POSITION_FIELD;
            }
        }
        return f;
    }

    @Unique
    private BlockPos paperarc$getTargetPosition() {
        try {
            return (BlockPos) paperarc$targetPositionField().get(getHandle());
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    @Unique
    private void paperarc$setTargetPosition(BlockPos pos) {
        try {
            paperarc$targetPositionField().set(getHandle(), pos);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to set NMS Bat.targetPosition", e);
        }
    }

    @Unique
    public Location getTargetLocation() {
        BlockPos pos = paperarc$getTargetPosition();
        if (pos == null) {
            return null;
        }
        return CraftLocation.toBukkit(pos, getHandle().level());
    }

    @Unique
    public void setTargetLocation(Location location) {
        BlockPos pos = location != null ? CraftLocation.toBlockPosition(location) : null;
        paperarc$setTargetPosition(pos);
    }
}
