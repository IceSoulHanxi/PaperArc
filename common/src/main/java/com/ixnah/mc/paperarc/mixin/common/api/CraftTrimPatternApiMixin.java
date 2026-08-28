package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v1_20_R1.inventory.trim.CraftTrimPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.world.item.armortrim.TrimPattern;

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
        String json = Serializer.toJson(this.getHandle().description());
        return GsonComponentSerializer.gson().deserialize(json);
    }
}
