package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-2：给 {@link org.bukkit.entity.Wolf} 补上 paper 声明的父接口 {@code io.papermc.paper.entity.CollarColorable}。
 */
@Mixin(targets = "org.bukkit.entity.Wolf", remap = false)
public interface WolfIfaceMixin extends io.papermc.paper.entity.CollarColorable {
}
