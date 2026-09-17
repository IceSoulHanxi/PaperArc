package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-2：给 {@link org.bukkit.command.ProxiedCommandSender} 补上 paper 声明的父接口
 * {@code net.kyori.adventure.audience.ForwardingAudience.Single}。
 */
@Mixin(targets = "org.bukkit.command.ProxiedCommandSender", remap = false)
public interface ProxiedCommandSenderIfaceMixin extends net.kyori.adventure.audience.ForwardingAudience.Single {

    /** paper {@code ProxiedCommandSender#audience()} 的 default 方法体：转发给调用者。 */
    @Unique
    public default net.kyori.adventure.audience.Audience audience() {
        return ((org.bukkit.command.ProxiedCommandSender) this).getCaller();
    }
}
