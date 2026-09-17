package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-2：给 {@link org.bukkit.block.Banner} 补上 paper 声明的父接口 {@code org.bukkit.Nameable}。
 */
@Mixin(targets = "org.bukkit.block.Banner", remap = false)
public interface BannerIfaceMixin extends org.bukkit.Nameable {
}
