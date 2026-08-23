package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.inventory.ItemType} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.inventory.ItemType", remap = false)
public interface ItemTypeIfaceMixin {

    @Unique
    public abstract com.google.common.collect.Multimap getDefaultAttributeModifiers();

    @Unique
    public abstract org.bukkit.inventory.ItemRarity getItemRarity();
}
