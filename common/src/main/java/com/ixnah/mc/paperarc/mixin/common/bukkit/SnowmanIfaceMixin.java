package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/**
 * A4-3 父接口差集：给 {@code org.bukkit.entity.Snowman} 补上 paper 声明的父接口
 * {@code com.destroystokyo.paper.entity.RangedEntity}。终端方法 rangedAttack/setChargingAttack 统一实现在 CraftMobApiMixin 上。
 *
 * <p>A4-3 父接口差集：补上 paper 声明的父接口 {@code io.papermc.paper.entity.Shearable}。终端方法 readyToBeSheared/shear 实现在 CraftShearableApiMixin 上。</p>
 */
@Mixin(targets = "org.bukkit.entity.Snowman", remap = false)
public interface SnowmanIfaceMixin extends com.destroystokyo.paper.entity.RangedEntity, io.papermc.paper.entity.Shearable {
}
