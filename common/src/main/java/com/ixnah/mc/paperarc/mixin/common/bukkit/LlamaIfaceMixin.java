package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Llama} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Llama", remap = false)
public interface LlamaIfaceMixin extends com.destroystokyo.paper.entity.RangedEntity {

    @Unique
    public abstract boolean inCaravan();

    @Unique
    public abstract void joinCaravan(org.bukkit.entity.Llama p0);

    @Unique
    public abstract void leaveCaravan();

    @Unique
    public abstract org.bukkit.entity.Llama getCaravanHead();

    @Unique
    public abstract boolean hasCaravanTail();

    @Unique
    public abstract org.bukkit.entity.Llama getCaravanTail();

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
