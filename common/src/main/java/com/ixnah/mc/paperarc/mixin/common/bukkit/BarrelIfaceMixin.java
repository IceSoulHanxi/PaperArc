package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/**
 * A4-3 父接口差集：给 {@code org.bukkit.block.Barrel} 补上 paper 声明的父接口
 * {@code com.destroystokyo.paper.loottable.LootableBlockInventory}。终端方法在 CraftLootableBlockApiMixin 上（自动补货在 Arclight 上不可用，见 docs/gaps.md）。
 */
@Mixin(targets = "org.bukkit.block.Barrel", remap = false)
public interface BarrelIfaceMixin extends com.destroystokyo.paper.loottable.LootableBlockInventory {
}
