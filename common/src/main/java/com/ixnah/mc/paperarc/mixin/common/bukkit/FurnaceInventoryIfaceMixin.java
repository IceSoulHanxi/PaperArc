package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.inventory.FurnaceInventory} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.inventory.FurnaceInventory", remap = false)
public interface FurnaceInventoryIfaceMixin {

    @Unique
    public abstract boolean canSmelt(org.bukkit.inventory.ItemStack p0);

    @Unique
    public abstract boolean isFuel(org.bukkit.inventory.ItemStack p0);

}