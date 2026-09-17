package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B2-6：给 {@link org.bukkit.inventory.LlamaInventory} 补上 paper 声明的父接口 {@code org.bukkit.inventory.SaddledHorseInventory}。
 * SaddledHorseInventory 是 RuntimeClassInjector 注入的类型（injections.json），本身没有抽象方法。
 */
@Mixin(targets = "org.bukkit.inventory.LlamaInventory", remap = false)
public interface LlamaInventoryIfaceMixin extends org.bukkit.inventory.SaddledHorseInventory {
}
