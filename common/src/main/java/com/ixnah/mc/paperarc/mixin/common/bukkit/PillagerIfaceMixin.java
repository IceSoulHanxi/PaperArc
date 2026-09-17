package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-2：给 {@link org.bukkit.entity.Pillager} 补上 paper 声明的父接口
 * {@code com.destroystokyo.paper.entity.RangedEntity}。
 */
@Mixin(targets = "org.bukkit.entity.Pillager", remap = false)
public interface PillagerIfaceMixin extends com.destroystokyo.paper.entity.RangedEntity {

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
