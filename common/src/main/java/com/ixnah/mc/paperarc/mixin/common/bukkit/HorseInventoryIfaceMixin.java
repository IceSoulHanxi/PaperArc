package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/**
 * A4-3 父接口差集：给 {@code org.bukkit.inventory.HorseInventory} 补上 paper 声明的父接口
 * {@code org.bukkit.inventory.ArmoredHorseInventory}。父接口的方法运行时本来就有（这两个父类型本身由 RuntimeClassInjector 注入），只补继承关系。
 */
@Mixin(targets = "org.bukkit.inventory.HorseInventory", remap = false)
public interface HorseInventoryIfaceMixin extends org.bukkit.inventory.ArmoredHorseInventory {
}
