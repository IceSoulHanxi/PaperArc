package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.generator.LimitedRegion} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.generator.LimitedRegion", remap = false)
public interface LimitedRegionIfaceMixin {

    @Unique
    public abstract void setBlockState(int p0, int p1, int p2, org.bukkit.block.BlockState p3);

    @Unique
    public abstract void scheduleBlockUpdate(int p0, int p1, int p2);

    @Unique
    public abstract void scheduleFluidUpdate(int p0, int p1, int p2);

    @Unique
    public abstract org.bukkit.World getWorld();

    @Unique
    public abstract int getCenterChunkX();

    @Unique
    public abstract int getCenterChunkZ();
}
