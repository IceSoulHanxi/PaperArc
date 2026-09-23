package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.inventory.trim.CraftTrimPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.craftbukkit.v.util.CraftChatMessage.ChatSerializer;
// alias: ChatSerializer
import net.minecraft.world.item.equipment.trim.TrimPattern;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;

/**
 * Adds Adventure description missing from Arclight CraftBukkit.
 * Paper ref: patches/server/Adventure.patch + Improve-Registry.patch
 * (PaperAdventure.asAdventure(handle.description()); gson round-trip used instead).
 */
@Mixin(CraftTrimPattern.class)
public abstract class CraftTrimPatternApiMixin {

    @Shadow
    public abstract TrimPattern getHandle();

    @Unique
    public Component description() {
        String json = ChatSerializer.toJson(this.getHandle().description(), ((org.bukkit.craftbukkit.v.CraftServer) PaperArcBridge.getServer()).getServer().registryAccess());
        return GsonComponentSerializer.gson().deserialize(json);
    }
}
