package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.AbstractSkeleton} (generated).
 * Adds 3 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.AbstractSkeleton", remap = false)
public interface AbstractSkeletonIfaceMixin extends com.destroystokyo.paper.entity.RangedEntity {

    @Unique
    public abstract boolean shouldBurnInDay();

    @Unique
    public abstract void setShouldBurnInDay(boolean p0);

    /**
     * paper {@code CraftRangedEntity} 的 default 方法体（B3-2）。实现体在
     * {@code bridge.api.PaperarcEntityTraits}，见那里的类注释。
     */
    @Unique
    public default void rangedAttack(org.bukkit.entity.LivingEntity target, float charge) {
        com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.rangedAttack(this, target, charge);
    }

    @Unique
    public default void setChargingAttack(boolean raiseHands) {
        com.ixnah.mc.paperarc.bridge.api.PaperarcEntityTraits.setChargingAttack(this, raiseHands);
    }
}
