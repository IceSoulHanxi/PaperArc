package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.inventory.view.BrewingStandView} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.inventory.view.BrewingStandView", remap = false)
public interface BrewingStandViewIfaceMixin {

    @Unique
    public abstract void setRecipeBrewTime(int p0);

    @Unique
    public abstract int getRecipeBrewTime();
}
