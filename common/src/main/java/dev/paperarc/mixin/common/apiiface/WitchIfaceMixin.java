package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Witch} (generated).
 * Adds 4 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Witch", remap = false)
public interface WitchIfaceMixin {

    public abstract int getPotionUseTimeLeft();

    public abstract void setPotionUseTimeLeft(int p0);

    public abstract org.bukkit.inventory.ItemStack getDrinkingPotion();

    public abstract void setDrinkingPotion(org.bukkit.inventory.ItemStack p0);
}
