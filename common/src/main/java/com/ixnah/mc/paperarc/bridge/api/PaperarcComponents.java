package com.ixnah.mc.paperarc.bridge.api;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.Component.Serializer;

/**
 * vanilla {@code Component} ↔ adventure {@code Component} 的 gson 往返转换。
 * PaperAdventure 在本运行时不可用，沿用各 api mixin 已在用的做法，集中一份。
 */
public final class PaperarcComponents {

    private PaperarcComponents() {
    }

    public static net.kyori.adventure.text.Component fromVanilla(net.minecraft.network.chat.Component vanilla) {
        String json = Serializer.toJson(vanilla, registries());
        return GsonComponentSerializer.gson().deserialize(json);
    }

    public static net.minecraft.network.chat.Component toVanilla(net.kyori.adventure.text.Component component) {
        String json = GsonComponentSerializer.gson().serialize(component);
        return Serializer.fromJson(json, registries());
    }

    /**
     * paper {@code UnsafeValues#resolveWithContext} 的实现体：照 Paper 走 vanilla 的
     * {@code ComponentUtils#updateForEntity}（展开选择器、记分板占位等）。
     * {@code CommandSender → CommandSourceStack} 用 CraftBukkit 自带的
     * {@code VanillaCommandWrapper#getListener}；解析失败（选择器语法错、匹配不到实体）
     * 按 Paper 抛 {@code IllegalArgumentException}。
     */
    public static net.kyori.adventure.text.Component resolveWithContext(
            net.kyori.adventure.text.Component component, org.bukkit.command.CommandSender context,
            org.bukkit.entity.Entity scoreboardSubject, boolean bypassPermissions) {
        if (component == null) {
            return null;
        }
        net.minecraft.commands.CommandSourceStack stack = context == null ? null
                : org.bukkit.craftbukkit.v.command.VanillaCommandWrapper.getListener(context);
        if (stack == null) {
            return component;
        }
        if (bypassPermissions) {
            stack = stack.withPermission(2);
        }
        net.minecraft.world.entity.Entity subject = scoreboardSubject == null ? null
                : ((org.bukkit.craftbukkit.v.entity.CraftEntity) scoreboardSubject).getHandle();
        try {
            return fromVanilla(net.minecraft.network.chat.ComponentUtils.updateForEntity(
                    stack, toVanilla(component), subject, 0));
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    private static net.minecraft.core.HolderLookup.Provider registries() {
        return ((org.bukkit.craftbukkit.v.CraftServer) PaperArcBridge.getServer()).getServer().registryAccess();
    }
}
