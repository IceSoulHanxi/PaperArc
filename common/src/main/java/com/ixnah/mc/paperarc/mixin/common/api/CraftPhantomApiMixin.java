package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.ApiState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.Phantom;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.entity.CraftPhantom;
import org.bukkit.craftbukkit.v.util.CraftLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's phantom APIs to {@link CraftPhantom}.
 *
 * <p>{@code Phantom.anchorPoint} is a package-private vanilla field with no
 * accessor, so it is accessed reflectively (mojmap runtime name:
 * {@code anchorPoint}).</p>
 *
 * <p>{@code shouldBurnInDay} and {@code spawningEntity} live in fields added by
 * Paper's server patches which do not exist in this Arclight-based runtime, so
 * they are stored side-map style in {@link ApiState} keyed by the NMS entity
 * ({@code shouldBurnInDay} defaults to {@code true}, {@code spawningEntity} to
 * {@code null}, both matching Paper's defaults).</p>
 */
@Mixin(CraftPhantom.class)
public abstract class CraftPhantomApiMixin {

    @Shadow
    public abstract Phantom getHandle();

    @Unique
    private static final String PAPERARC$SHOULD_BURN_KEY = "shouldBurnInDay";

    @Unique
    private static final String PAPERARC$SPAWNING_ENTITY_KEY = "spawningEntity";

    @Unique
    private static volatile java.lang.reflect.Field PAPERARC$ANCHOR_POINT_FIELD;

    @Unique
    private static java.lang.reflect.Field paperarc$anchorPointField() {
        java.lang.reflect.Field f = PAPERARC$ANCHOR_POINT_FIELD;
        if (f == null) {
            synchronized (CraftPhantomApiMixin.class) {
                if (PAPERARC$ANCHOR_POINT_FIELD == null) {
                    try {
                        java.lang.reflect.Field resolved = Phantom.class.getDeclaredField("anchorPoint");
                        resolved.setAccessible(true);
                        PAPERARC$ANCHOR_POINT_FIELD = resolved;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("NMS Phantom.anchorPoint field not found", e);
                    }
                    f = PAPERARC$ANCHOR_POINT_FIELD;
                }
            }
        }
        return f;
    }

    @Unique
    private BlockPos paperarc$getAnchorPoint() {
        try {
            return (BlockPos) paperarc$anchorPointField().get(getHandle());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to read NMS Phantom.anchorPoint", e);
        }
    }

    @Unique
    private void paperarc$setAnchorPoint(BlockPos pos) {
        try {
            paperarc$anchorPointField().set(getHandle(), pos);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to write NMS Phantom.anchorPoint", e);
        }
    }

    @Unique
    public java.util.UUID getSpawningEntity() {
        return ApiState.get(getHandle(), PAPERARC$SPAWNING_ENTITY_KEY, (java.util.UUID) null);
    }

    @Unique
    public boolean shouldBurnInDay() {
        return ApiState.get(getHandle(), PAPERARC$SHOULD_BURN_KEY, Boolean.TRUE);
    }

    @Unique
    public void setShouldBurnInDay(boolean shouldBurnInDay) {
        ApiState.put(getHandle(), PAPERARC$SHOULD_BURN_KEY, shouldBurnInDay);
    }

    @Unique
    public Location getAnchorLocation() {
        return CraftLocation.toBukkit(paperarc$getAnchorPoint(), getHandle().level());
    }

    @Unique
    public void setAnchorLocation(Location location) {
        com.google.common.base.Preconditions.checkArgument(location != null, "location cannot be null");
        paperarc$setAnchorPoint(CraftLocation.toBlockPosition(location));
    }
}
