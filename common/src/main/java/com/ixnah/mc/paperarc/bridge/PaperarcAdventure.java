package com.ixnah.mc.paperarc.bridge;

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

/**
 * NMS {@code Component} ↔ adventure {@code Component} 的互转。
 *
 * <p>Paper 用的 {@code io.papermc.paper.adventure.PaperAdventure} 在 Arclight 运行时不存在，
 * 仓库里已有几处（{@code CraftTextDisplayApiMixin}、{@code CraftMinecartCommandApiMixin}）
 * 各写一遍 gson 往返，这里收拢成一处，给 A8 新增的
 * {@code PlayerAdvancementDoneEvent#message()} 与 {@code InventoryOpenEvent#titleOverride()} 用。
 */
public final class PaperarcAdventure {

    private PaperarcAdventure() {
    }

    public static net.kyori.adventure.text.Component asAdventure(net.minecraft.network.chat.Component vanilla) {
        return vanilla == null ? null
                : GsonComponentSerializer.gson().deserialize(
                        net.minecraft.network.chat.Component.Serializer.toJson(vanilla));
    }

    public static net.minecraft.network.chat.Component asVanilla(net.kyori.adventure.text.Component adventure) {
        return adventure == null ? null
                : net.minecraft.network.chat.Component.Serializer.fromJson(
                        GsonComponentSerializer.gson().serialize(adventure));
    }
}
