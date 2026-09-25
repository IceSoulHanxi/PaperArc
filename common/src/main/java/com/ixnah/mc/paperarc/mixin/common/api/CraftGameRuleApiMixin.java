package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.translation.Translatable;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.bukkit.NamespacedKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Implements adventure {@link Translatable} on CraftGameRule (checklist §1.12 bb).
 */
@Mixin(targets = "org.bukkit.craftbukkit.v.CraftGameRule", remap = false)
public abstract class CraftGameRuleApiMixin implements Translatable {

    @Shadow
    public abstract NamespacedKey getKey();

    @Unique
    @Override
    public String translationKey() {
        NamespacedKey key = this.getKey();
        return Util.makeDescriptionId("gamerule", Identifier.fromNamespaceAndPath(key.getNamespace(), key.getKey()));
    }
}
