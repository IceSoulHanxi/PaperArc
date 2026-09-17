package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/**
 * A4-3 父接口差集：给 {@code org.bukkit.inventory.LlamaInventory} 补上 paper 声明的父接口
 * {@code org.bukkit.inventory.SaddledHorseInventory}。父接口的方法运行时本来就有（这两个父类型本身由 RuntimeClassInjector 注入），只补继承关系。
 */
@Mixin(targets = "org.bukkit.inventory.LlamaInventory", remap = false)
public interface LlamaInventoryIfaceMixin extends org.bukkit.inventory.SaddledHorseInventory {
}
