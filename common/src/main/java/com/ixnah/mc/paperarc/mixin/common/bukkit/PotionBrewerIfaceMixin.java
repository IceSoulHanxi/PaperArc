package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.potion.PotionBrewer} (generated).
 * Adds 3 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.potion.PotionBrewer", remap = false)
public interface PotionBrewerIfaceMixin {

    @Unique
    public abstract void addPotionMix(io.papermc.paper.potion.PotionMix p0);

    @Unique
    public abstract void removePotionMix(org.bukkit.NamespacedKey p0);

    @Unique
    public abstract void resetPotionMixes();

}