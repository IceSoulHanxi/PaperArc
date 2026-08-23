package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.inventory.view.AnvilView} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.inventory.view.AnvilView", remap = false)
public interface AnvilViewIfaceMixin {

    @Unique
    public abstract boolean bypassesEnchantmentLevelRestriction();

    @Unique
    public abstract void bypassEnchantmentLevelRestriction(boolean p0);
}
