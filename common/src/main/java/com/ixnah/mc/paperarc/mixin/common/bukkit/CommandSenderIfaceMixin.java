package com.ixnah.mc.paperarc.mixin.common.bukkit;

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
}
