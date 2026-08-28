package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.ApiState;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftAbstractSkeleton;
import org.bukkit.entity.Skeleton;
import com.ixnah.mc.paperarc.bridge.craft.CraftEntityBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's AbstractSkeleton API on {@link CraftAbstractSkeleton}:
 * {@code getSkeletonType()} plus the should-burn-in-day accessors.
 *
 * Paper stores {@code shouldBurnInDay} in an NMS field added by their patch
 * (with NBT persistence); this codebase's vanilla-based NMS has no such
 * field, so the value lives in the ApiState side map keyed by the NMS handle
 * (default {@code true}, matching vanilla burn-in-sunlight behaviour).
 */
@Mixin(CraftAbstractSkeleton.class)
public abstract class CraftAbstractSkeletonApiMixin {

    @Unique
    private static final String PAPERARC_BURN_KEY = "paperarc:shouldBurnInDay";

    /** narrowed covariant handle */
    @Unique
    private AbstractSkeleton getHandle() {
        return (AbstractSkeleton) ((CraftEntityBridge) (Object) this).paperarc$getHandle();
    }

    @Unique
    public Skeleton.SkeletonType getSkeletonType() {
        AbstractSkeleton handle = this.getHandle();
        if (handle instanceof net.minecraft.world.entity.monster.WitherSkeleton) {
            return Skeleton.SkeletonType.WITHER;
        } else if (handle instanceof net.minecraft.world.entity.monster.Stray) {
            return Skeleton.SkeletonType.STRAY;
        }
        return Skeleton.SkeletonType.NORMAL;
    }

    @Unique
    public boolean shouldBurnInDay() {
        return ApiState.get(this.getHandle(), PAPERARC_BURN_KEY, Boolean.TRUE);
    }

    @Unique
    public void setShouldBurnInDay(boolean shouldBurnInDay) {
        ApiState.put(this.getHandle(), PAPERARC_BURN_KEY, shouldBurnInDay);
    }
}
