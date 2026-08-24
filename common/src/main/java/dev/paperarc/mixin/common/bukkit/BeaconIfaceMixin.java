package dev.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.Beacon} (generated).
 * Adds 3 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.block.Beacon", remap = false)
public interface BeaconIfaceMixin {

    @Unique
    public abstract double getEffectRange();

    @Unique
    public abstract void setEffectRange(double p0);

    @Unique
    public abstract void resetEffectRange();
}
