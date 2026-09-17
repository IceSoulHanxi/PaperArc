package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Llama} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 *
 * <p>A4-3 父接口差集：补上 paper 声明的父接口 {@code com.destroystokyo.paper.entity.RangedEntity}。终端方法 rangedAttack/setChargingAttack 统一实现在 CraftMobApiMixin 上。</p>
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
}
