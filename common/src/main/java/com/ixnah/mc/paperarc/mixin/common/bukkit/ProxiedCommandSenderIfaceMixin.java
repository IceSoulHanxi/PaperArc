package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * A4-3 父接口差集：给 {@code org.bukkit.command.ProxiedCommandSender} 补上 paper 声明的父接口
 * {@code net.kyori.adventure.audience.ForwardingAudience.Single}。终端方法 audience() 在 ProxiedNativeCommandSenderApiMixin 上。
 */
@Mixin(targets = "org.bukkit.command.ProxiedCommandSender", remap = false)
public interface ProxiedCommandSenderIfaceMixin extends net.kyori.adventure.audience.ForwardingAudience.Single {
    // ===== A4-4：paper-api 的 default 方法体照搬（运行时接口里一个都没有）=====

    @Unique
    public default net.kyori.adventure.audience.Audience audience() {
        return ((org.bukkit.command.ProxiedCommandSender) this).getCallee();
    }

}
