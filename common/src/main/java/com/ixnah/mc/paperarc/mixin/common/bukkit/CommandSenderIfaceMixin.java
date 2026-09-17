package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.command.CommandSender;
import java.util.UUID;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Server;
import org.bukkit.permissions.Permissible;
import net.kyori.adventure.audience.Audience;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.command.CommandSender}.
 *
 * <p>paper-api 的 {@code CommandSender extends Audience}，Arclight 运行时只有
 * {@code Permissible} —— 插件 {@code (Audience) sender} 即 ClassCastException、
 * {@code sender.sendMessage(Component)} 即 NoSuchMethodError。这里靠 Mixin 的
 * {@code MixinApplicatorInterface#applyInterfaces} 把 mixin 自身的父接口合并到接口
 * 目标上（Phase A2-1 在 1.20.1 真机实测 {@code isAssignableFrom=true}）。
 *
 * <p><b>只合并父接口等于没做事</b>：adventure {@code Audience} 的方法体全是空 default，
 * 插件调用不报错、消息静默丢弃。必须同时在 Craft 实现类上覆盖"终端方法"——
 * 见 {@code ServerCommandSenderApiMixin}（控制台/命令方块/RCON）、
 * {@code ProxiedNativeCommandSenderApiMixin}、{@code CraftPlayerApiMixin}。
 */
@Mixin(targets = "org.bukkit.command.CommandSender", remap = false)
public interface CommandSenderIfaceMixin extends Audience {

    @Unique
    public abstract net.kyori.adventure.text.Component name();

    @Unique
    public default void sendMessage(Identity identity, Component message, MessageType type) {
        CommandSender self = (CommandSender) this;
        self.sendMessage(LegacyComponentSerializer.legacySection().serialize(message));
    }

    @Unique
    public default void sendRichMessage(String message) {
        CommandSender self = (CommandSender) this;
        self.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    @Unique
    public default void sendRichMessage(String message, TagResolver... resolvers) {
        CommandSender self = (CommandSender) this;
        self.sendMessage(MiniMessage.miniMessage().deserialize(message, resolvers));
    }

    @Unique
    public default void sendPlainMessage(String message) {
        CommandSender self = (CommandSender) this;
        self.sendMessage((Component) Component.text(message));
    }

    @Unique
    public default void sendMessage(BaseComponent component) {
        CommandSender self = (CommandSender) this;
        self.sendMessage(component.toLegacyText());
    }

    @Unique
    public default void sendMessage(BaseComponent... components) {
        CommandSender self = (CommandSender) this;
        self.sendMessage((new TextComponent(components)).toLegacyText());
    }
}
