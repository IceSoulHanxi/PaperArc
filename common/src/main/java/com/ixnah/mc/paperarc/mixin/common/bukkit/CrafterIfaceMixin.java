package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-2：给 {@link org.bukkit.block.Crafter} 补上 paper 声明的父接口
 * {@code com.destroystokyo.paper.loottable.LootableBlockInventory}；
 * 实现体在 {@code api.CraftLootableInventoryApiMixin}。
 */
@Mixin(targets = "org.bukkit.block.Crafter", remap = false)
public interface CrafterIfaceMixin extends com.destroystokyo.paper.loottable.LootableBlockInventory {
}
