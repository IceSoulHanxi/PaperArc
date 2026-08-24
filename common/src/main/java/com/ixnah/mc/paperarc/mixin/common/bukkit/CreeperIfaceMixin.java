package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Creeper} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Creeper", remap = false)
public interface CreeperIfaceMixin {

    @Unique
    public abstract void setIgnited(boolean p0);

    @Unique
    public abstract boolean isIgnited();
}
