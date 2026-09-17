package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/**
 * A4-3 父接口差集：给 {@code org.bukkit.entity.Sheep} 补上 paper 声明的父接口
 * {@code io.papermc.paper.entity.Shearable}。终端方法 readyToBeSheared/shear 实现在 CraftShearableApiMixin 上。
 */
@Mixin(targets = "org.bukkit.entity.Sheep", remap = false)
public interface SheepIfaceMixin extends io.papermc.paper.entity.Shearable {
}
