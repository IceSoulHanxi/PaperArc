package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.RegionAccessor} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.RegionAccessor", remap = false)
public interface RegionAccessorIfaceMixin {

    @Unique
    public abstract org.bukkit.block.Biome getComputedBiome(int p0, int p1, int p2);

    @Unique
    public abstract io.papermc.paper.block.fluid.FluidData getFluidData(int p0, int p1, int p2);

    @Unique
    public abstract io.papermc.paper.world.MoonPhase getMoonPhase();

    @Unique
    public abstract org.bukkit.NamespacedKey getKey();

    @Unique
    public abstract boolean lineOfSightExists(org.bukkit.Location p0, org.bukkit.Location p1);

    @Unique
    public abstract boolean hasCollisionsIn(org.bukkit.util.BoundingBox p0);
}
