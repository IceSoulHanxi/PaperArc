package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.MushroomCow} (generated).
 * Adds 4 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.MushroomCow", remap = false)
public interface MushroomCowIfaceMixin {

    @Unique
    public abstract int getStewEffectDuration();

    @Unique
    public abstract org.bukkit.potion.PotionEffectType getStewEffectType();

    @Unique
    public abstract void setStewEffect(org.bukkit.potion.PotionEffectType p0);

    @Unique
    public abstract void setStewEffectDuration(int p0);

}