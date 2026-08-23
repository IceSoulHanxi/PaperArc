package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.inventory.MenuType} (generated).
 * Adds 1 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.inventory.MenuType", remap = false)
public interface MenuTypeIfaceMixin {

    public abstract org.bukkit.inventory.InventoryView create(org.bukkit.entity.HumanEntity p0, net.kyori.adventure.text.Component p1);
}
