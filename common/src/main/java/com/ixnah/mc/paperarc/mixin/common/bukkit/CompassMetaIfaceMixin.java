package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.inventory.meta.CompassMeta}.
 * Adds paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).
 */
@Mixin(targets = "org.bukkit.inventory.meta.CompassMeta", remap = false)
public interface CompassMetaIfaceMixin {

    @Unique
    public abstract void clearLodestone();

    @Unique
    public abstract boolean isLodestoneCompass();
}
