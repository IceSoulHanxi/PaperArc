package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-2：给 {@link org.bukkit.entity.minecart.StorageMinecart} 补上 paper 声明的父接口
 * {@code com.destroystokyo.paper.loottable.LootableEntityInventory}；
 * 实现体在 {@code api.CraftMinecartContainerLootableApiMixin} 与
 * {@code api.CraftChestBoatLootableApiMixin}。
 */
@Mixin(targets = "org.bukkit.entity.minecart.StorageMinecart", remap = false)
public interface StorageMinecartIfaceMixin extends com.destroystokyo.paper.loottable.LootableEntityInventory {
}
