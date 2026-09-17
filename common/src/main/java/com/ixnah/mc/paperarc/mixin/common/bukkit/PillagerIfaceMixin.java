package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/**
 * A4-3 父接口差集：给 {@code org.bukkit.entity.Pillager} 补上 paper 声明的父接口
 * {@code com.destroystokyo.paper.entity.RangedEntity}。终端方法 rangedAttack/setChargingAttack 统一实现在 CraftMobApiMixin 上。
 */
@Mixin(targets = "org.bukkit.entity.Pillager", remap = false)
public interface PillagerIfaceMixin extends com.destroystokyo.paper.entity.RangedEntity {
}
