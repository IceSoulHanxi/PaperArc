package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.inventory.MenuType.Typed}.
 * Adds paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).
 */
@Mixin(targets = "org.bukkit.inventory.MenuType$Typed", remap = false)
public interface MenuTypeTypedIfaceMixin {

    @Unique
    public abstract org.bukkit.inventory.InventoryView create(org.bukkit.entity.HumanEntity p0, net.kyori.adventure.text.Component p1);
}
