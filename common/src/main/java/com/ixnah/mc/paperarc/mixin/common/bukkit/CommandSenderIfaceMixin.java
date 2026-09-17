package com.ixnah.mc.paperarc.mixin.common.bukkit;

import net.kyori.adventure.audience.Audience;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.command.CommandSender} (generated).
 * Adds 1 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 *
 * <p>paper-api 的 {@code CommandSender extends Audience}，Arclight 运行时只有
 * {@code Permissible} —— 这里靠 MixinApplicatorInterface#applyInterfaces 把父接口
 * 合并到接口目标上（实验见 docs/execution-plan-2026-09-16.md A2-1）。
 */
@Mixin(targets = "org.bukkit.command.CommandSender", remap = false)
public interface CommandSenderIfaceMixin extends Audience {

    @Unique
    public abstract net.kyori.adventure.text.Component name();

    // ===== A4-4：paper-api 的 default 方法体照搬（运行时接口里一个都没有）=====

    @Unique
    public default void sendMessage(net.md_5.bungee.api.chat.BaseComponent component) {
        ((org.bukkit.command.CommandSender) this).spigot().sendMessage(component);
    }

    @Unique
    public default void sendMessage(net.md_5.bungee.api.chat.BaseComponent... components) {
        ((org.bukkit.command.CommandSender) this).spigot().sendMessage(components);
    }

    @Unique
    public default void sendPlainMessage(String message) {
        ((net.kyori.adventure.audience.Audience) this).sendMessage(
                net.kyori.adventure.text.Component.text(message));
    }

    @Unique
    public default void sendRichMessage(String message) {
        ((net.kyori.adventure.audience.Audience) this).sendMessage(
                net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(message));
    }

    @Unique
    public default void sendRichMessage(String message,
            net.kyori.adventure.text.minimessage.tag.resolver.TagResolver... resolvers) {
        ((net.kyori.adventure.audience.Audience) this).sendMessage(
                net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(message, resolvers));
    }

}
