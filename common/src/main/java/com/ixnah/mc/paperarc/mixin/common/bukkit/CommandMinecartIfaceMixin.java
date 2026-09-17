package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-2：给 {@link org.bukkit.entity.minecart.CommandMinecart} 补上 paper 声明的父接口 {@code io.papermc.paper.command.CommandBlockHolder}。
 */
@Mixin(targets = "org.bukkit.entity.minecart.CommandMinecart", remap = false)
public interface CommandMinecartIfaceMixin extends io.papermc.paper.command.CommandBlockHolder {
}
