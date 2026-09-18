package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/**
 * A5-1 父接口差集：给 {@link org.bukkit.block.Banner} 补上 paper 声明的父接口
 * {@code org.bukkit.Nameable}。
 *
 * <p>{@code customName()}/{@code customName(Component)} 由所有方块状态共用的
 * {@code CraftBlockEntityStateNameableApiMixin} 覆盖；String 版的
 * {@code getCustomName}/{@code setCustomName} 是 spigot 侧按类逐个实现的，
 * 横幅没有，补在 {@code CraftBannerNameableApiMixin} 上。</p>
 */
@Mixin(targets = "org.bukkit.block.Banner", remap = false)
public interface BannerIfaceMixin extends org.bukkit.Nameable {
}
