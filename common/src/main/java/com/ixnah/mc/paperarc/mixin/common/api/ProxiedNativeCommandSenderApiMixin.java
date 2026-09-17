package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v.command.ProxiedNativeCommandSender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Audience 落地端（{@code /execute as} 之类的代理发送者）。
 * {@code ProxiedNativeCommandSender} 不继承 {@code ServerCommandSender}，
 * 需要单独补；语义与它的 {@code sendMessage(String)} 一致 —— 转发给 callee。
 */
@Mixin(ProxiedNativeCommandSender.class)
public abstract class ProxiedNativeCommandSenderApiMixin {

    @Shadow
    public abstract CommandSender getCallee();

    @Unique
    public Component name() {
        return Component.text(((CommandSender) (Object) this).getName());
    }

    @Unique
    public void sendMessage(Identity source, Component message, MessageType type) {
        if (message == null) {
            return;
        }
        getCallee().sendMessage(source, message, type);
    }
    // audience() 改为写在 ProxiedCommandSenderIfaceMixin 的 default 方法体上（A4-4）。

}
