package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.LightningStrike} (generated).
 * Adds 3 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.LightningStrike", remap = false)
public interface LightningStrikeIfaceMixin {

    @Unique
    public abstract int getFlashCount();

    @Unique
    public abstract void setFlashCount(int p0);

    @Unique
    public abstract org.bukkit.entity.Entity getCausingEntity();
}
