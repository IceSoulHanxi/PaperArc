package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.RegionAccessor} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.RegionAccessor", remap = false)
public interface RegionAccessorIfaceMixin extends io.papermc.paper.world.flag.FeatureFlagSetHolder, org.bukkit.Keyed {

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

    /**
     * 实现体是运行时 CraftBukkit 自带的（CraftRegionAccessor 上已有同签名方法），
     * 只有运行时**接口**少了这条声明，插件按接口调用才会 NoSuchMethodError。
     */
    @Unique
    public abstract org.bukkit.entity.Entity spawn(org.bukkit.Location p0, java.lang.Class p1, java.util.function.Consumer p2, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason p3);
}
