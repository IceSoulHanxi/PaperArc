package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Piglin} (generated).
 * Adds 4 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 *
 * <p>A4-3 父接口差集：补上 paper 声明的父接口 {@code com.destroystokyo.paper.entity.RangedEntity}。终端方法 rangedAttack/setChargingAttack 统一实现在 CraftMobApiMixin 上。</p>
 */
@Mixin(targets = "org.bukkit.entity.Piglin", remap = false)
public interface PiglinIfaceMixin extends com.destroystokyo.paper.entity.RangedEntity {

    @Unique
    public abstract void setChargingCrossbow(boolean p0);

    @Unique
    public abstract boolean isChargingCrossbow();

    @Unique
    public abstract void setDancing(boolean p0);

    @Unique
    public abstract boolean isDancing();
}
