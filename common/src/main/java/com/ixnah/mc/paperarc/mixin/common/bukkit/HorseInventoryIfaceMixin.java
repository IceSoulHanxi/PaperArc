package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-2：给 {@link org.bukkit.inventory.HorseInventory} 补上 paper 声明的父接口 {@code org.bukkit.inventory.ArmoredHorseInventory}。
 */
@Mixin(targets = "org.bukkit.inventory.HorseInventory", remap = false)
public interface HorseInventoryIfaceMixin extends org.bukkit.inventory.ArmoredHorseInventory {
}
