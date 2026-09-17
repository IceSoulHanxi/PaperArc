package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/**
 * A4-3 父接口差集：给 {@code org.bukkit.entity.Wolf} 补上 paper 声明的父接口
 * {@code io.papermc.paper.entity.CollarColorable}。父接口的两个方法运行时本来就有，只补继承关系。
 */
@Mixin(targets = "org.bukkit.entity.Wolf", remap = false)
public interface WolfIfaceMixin extends io.papermc.paper.entity.CollarColorable {
}
