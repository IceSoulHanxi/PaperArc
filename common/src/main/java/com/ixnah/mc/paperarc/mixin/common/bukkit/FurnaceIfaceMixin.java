package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.Furnace} (generated).
 * Adds 5 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.block.Furnace", remap = false)
public interface FurnaceIfaceMixin {

    @Unique
    public abstract double getCookSpeedMultiplier();

    @Unique
    public abstract void setCookSpeedMultiplier(double p0);

    @Unique
    public abstract int getRecipeUsedCount(org.bukkit.NamespacedKey p0);

    @Unique
    public abstract boolean hasRecipeUsedCount(org.bukkit.NamespacedKey p0);

    @Unique
    public abstract void setRecipeUsedCount(org.bukkit.inventory.CookingRecipe p0, int p1);
}
