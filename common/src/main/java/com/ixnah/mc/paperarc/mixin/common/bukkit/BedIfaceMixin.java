package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.data.type.Bed} (generated).
 * Adds 4 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.block.data.type.Bed", remap = false)
public interface BedIfaceMixin {

    @Unique
    public abstract org.bukkit.block.data.type.Bed.Part getPart();

    @Unique
    public abstract void setPart(org.bukkit.block.data.type.Bed.Part p0);

    @Unique
    public abstract boolean isOccupied();

    @Unique
    public abstract void setOccupied(boolean p0);
}
