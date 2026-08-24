package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Witch} (generated).
 * Adds 4 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Witch", remap = false)
public interface WitchIfaceMixin {

    @Unique
    public abstract int getPotionUseTimeLeft();

    @Unique
    public abstract void setPotionUseTimeLeft(int p0);

    @Unique
    public abstract org.bukkit.inventory.ItemStack getDrinkingPotion();

    @Unique
    public abstract void setDrinkingPotion(org.bukkit.inventory.ItemStack p0);
}
