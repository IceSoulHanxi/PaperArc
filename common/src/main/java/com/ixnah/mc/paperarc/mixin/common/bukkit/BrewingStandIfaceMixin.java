package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.BrewingStand} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.block.BrewingStand", remap = false)
public interface BrewingStandIfaceMixin {

    @Unique
    public abstract void setRecipeBrewTime(int p0);

    @Unique
    public abstract int getRecipeBrewTime();
}
