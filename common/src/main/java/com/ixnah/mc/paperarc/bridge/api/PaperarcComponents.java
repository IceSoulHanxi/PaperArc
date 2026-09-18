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

    private static net.minecraft.core.HolderLookup.Provider registries() {
        return ((org.bukkit.craftbukkit.v.CraftServer) PaperArcBridge.getServer()).getServer().registryAccess();
    }
}
