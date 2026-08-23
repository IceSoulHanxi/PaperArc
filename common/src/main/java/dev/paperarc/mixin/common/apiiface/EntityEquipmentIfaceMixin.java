package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.inventory.EntityEquipment} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.inventory.EntityEquipment", remap = false)
public interface EntityEquipmentIfaceMixin {

    public abstract float getDropChance(org.bukkit.inventory.EquipmentSlot p0);

    public abstract void setDropChance(org.bukkit.inventory.EquipmentSlot p0, float p1);
}
