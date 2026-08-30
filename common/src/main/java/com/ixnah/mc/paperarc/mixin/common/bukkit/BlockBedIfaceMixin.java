package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.Bed} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.block.Bed", remap = false)
public interface BlockBedIfaceMixin {

    @Unique
    public abstract org.bukkit.DyeColor getColor();

    @Unique
    public abstract void setColor(org.bukkit.DyeColor p0);

}