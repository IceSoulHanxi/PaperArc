package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.AbstractSkeletonBridge;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.bukkit.craftbukkit.v.entity.CraftAbstractSkeleton;
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
 * (with NBT persistence); that field is injected into the NMS
 * {@code AbstractSkeleton} by {@code AbstractSkeletonFieldsMixin} and reached
 * through {@link com.ixnah.mc.paperarc.bridge.AbstractSkeletonBridge} (default
 * {@code true}, matching vanilla burn-in-sunlight behaviour).
 */
@Mixin(CraftAbstractSkeleton.class)
public abstract class CraftAbstractSkeletonApiMixin {

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
        return ((com.ixnah.mc.paperarc.bridge.AbstractSkeletonBridge) this.getHandle()).shouldBurnInDay();
    }

    @Unique
    public void setShouldBurnInDay(boolean shouldBurnInDay) {
        ((com.ixnah.mc.paperarc.bridge.AbstractSkeletonBridge) this.getHandle()).setShouldBurnInDay(shouldBurnInDay);
    }
}
