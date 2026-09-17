package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-2：给 {@link org.bukkit.block.Dispenser} 补上 paper 声明的父接口
 * {@code com.destroystokyo.paper.loottable.LootableBlockInventory}；
 * 实现体在 {@code api.CraftLootableInventoryApiMixin}。
 */
@Mixin(targets = "org.bukkit.block.Dispenser", remap = false)
public interface DispenserIfaceMixin extends com.destroystokyo.paper.loottable.LootableBlockInventory {
}
