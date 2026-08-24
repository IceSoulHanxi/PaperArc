package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.inventory.meta.trim.TrimMaterial} (generated).
 * Adds 1 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.inventory.meta.trim.TrimMaterial", remap = false)
public interface TrimMaterialIfaceMixin {

    @Unique
    public abstract net.kyori.adventure.text.Component description();
}
