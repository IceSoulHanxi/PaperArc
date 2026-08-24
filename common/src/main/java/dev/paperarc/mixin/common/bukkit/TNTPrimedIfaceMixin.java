package dev.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.TNTPrimed} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.TNTPrimed", remap = false)
public interface TNTPrimedIfaceMixin {

    @Unique
    public abstract void setBlockData(org.bukkit.block.data.BlockData p0);

    @Unique
    public abstract org.bukkit.block.data.BlockData getBlockData();
}
