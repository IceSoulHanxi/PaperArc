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

}
