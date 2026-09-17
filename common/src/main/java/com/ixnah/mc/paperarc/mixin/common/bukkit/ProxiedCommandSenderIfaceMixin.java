package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/**
 * A4-3 父接口差集：给 {@code org.bukkit.command.ProxiedCommandSender} 补上 paper 声明的父接口
 * {@code net.kyori.adventure.audience.ForwardingAudience.Single}。终端方法 audience() 在 ProxiedNativeCommandSenderApiMixin 上。
 */
@Mixin(targets = "org.bukkit.command.ProxiedCommandSender", remap = false)
public interface ProxiedCommandSenderIfaceMixin extends net.kyori.adventure.audience.ForwardingAudience.Single {
}
