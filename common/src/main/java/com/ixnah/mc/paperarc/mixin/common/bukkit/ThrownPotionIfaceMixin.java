package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.ThrownPotion} (generated).
 * Adds 3 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.ThrownPotion", remap = false)
public interface ThrownPotionIfaceMixin {

    @Unique
    public abstract org.bukkit.inventory.meta.PotionMeta getPotionMeta();

    @Unique
    public abstract void setPotionMeta(org.bukkit.inventory.meta.PotionMeta p0);

    @Unique
    public abstract void splash();
}
