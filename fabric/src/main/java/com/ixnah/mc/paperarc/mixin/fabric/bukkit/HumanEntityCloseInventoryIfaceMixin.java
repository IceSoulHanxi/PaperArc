package com.ixnah.mc.paperarc.mixin.fabric.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Fabric-side declaration of Paper's HumanEntity#closeInventory(Reason).
 * Kept out of the common config because the bukkit inner-enum descriptor is
 * unresolvable during NeoForge's early mixin apply of CraftHumanEntity.
 */
@Mixin(targets = "org.bukkit.entity.HumanEntity", remap = false)
public interface HumanEntityCloseInventoryIfaceMixin {

    @Unique
    public abstract void closeInventory(org.bukkit.event.inventory.InventoryCloseEvent.Reason p0);
}
