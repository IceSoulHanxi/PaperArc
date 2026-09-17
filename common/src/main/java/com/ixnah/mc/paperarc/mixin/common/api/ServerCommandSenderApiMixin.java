package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v.command.ServerCommandSender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Audience 落地端（控制台 / 命令方块 / RCON）：{@code ServerCommandSender} 是
 * CraftBukkit 侧所有非玩家 CommandSender 的抽象基类
 * （CraftConsoleCommandSender、ColouredConsoleSender、CraftBlockCommandSender、
 * CraftRemoteConsoleCommandSender 均继承它）。
 *
 * <p>{@code CommandSenderIfaceMixin extends Audience} 只把父接口并进运行时的
 * {@code org.bukkit.command.CommandSender}，Audience 的方法体全是空 default ——
 * 不落地这里，插件的 {@code sender.sendMessage(Component)} 会静默丢消息。
 * 所有 sendMessage 重载最终都汇聚到
 * {@code sendMessage(Identity, Component, MessageType)}（javap 核对 adventure
 * Audience 字节码），因此只需覆盖这一个终端方法。
 *
 * <p>类里没有 {@code sendMessage(String)} 的 @Shadow —— 该方法由各具体子类声明，
 * 基类只继承接口声明，@Shadow 会报 "was not located in the target class"
 * （见状态文档第五章）；改用接口转型调用。
 */
@Mixin(ServerCommandSender.class)
public abstract class ServerCommandSenderApiMixin {

    @Unique
    public Component name() {
        return Component.text(((CommandSender) (Object) this).getName());
    }

    @Unique
    public void sendMessage(Identity source, Component message, MessageType type) {
        if (message == null) {
            return;
        }
        ((CommandSender) (Object) this).sendMessage(
                LegacyComponentSerializer.legacySection().serialize(message));
    }
}
