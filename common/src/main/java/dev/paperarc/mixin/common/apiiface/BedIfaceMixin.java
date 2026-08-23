package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.data.type.Bed} (generated).
 * Adds 4 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.block.data.type.Bed", remap = false)
public interface BedIfaceMixin {

    public abstract org.bukkit.block.data.type.Bed.Part getPart();

    public abstract void setPart(org.bukkit.block.data.type.Bed.Part p0);

    public abstract boolean isOccupied();

    public abstract void setOccupied(boolean p0);
}
