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
    // ===== A4-4：paper-api 的 default 方法体照搬（运行时接口里一个都没有）=====

    @Unique
    public default int getCenterBlockX() {
        return (getCenterChunkX() << 4) + 8;
    }

    @Unique
    public default int getCenterBlockZ() {
        return (getCenterChunkZ() << 4) + 8;
    }

    @Unique
    public default org.bukkit.block.data.BlockData getBlockData(org.bukkit.util.Vector position) {
        org.bukkit.generator.LimitedRegion self = (org.bukkit.generator.LimitedRegion) this;
        return self.getBlockData(position.getBlockX(), position.getBlockY(), position.getBlockZ());
    }

    @Unique
    public default void setBlockData(org.bukkit.util.Vector position, org.bukkit.block.data.BlockData data) {
        org.bukkit.generator.LimitedRegion self = (org.bukkit.generator.LimitedRegion) this;
        self.setBlockData(position.getBlockX(), position.getBlockY(), position.getBlockZ(), data);
    }

    @Unique
    public default org.bukkit.block.BlockState getBlockState(org.bukkit.util.Vector position) {
        org.bukkit.generator.LimitedRegion self = (org.bukkit.generator.LimitedRegion) this;
        return self.getBlockState(position.getBlockX(), position.getBlockY(), position.getBlockZ());
    }

    @Unique
    public default void setBlockState(org.bukkit.util.Vector position, org.bukkit.block.BlockState state) {
        setBlockState(position.getBlockX(), position.getBlockY(), position.getBlockZ(), state);
    }

    @Unique
    public default void scheduleBlockUpdate(org.bukkit.util.Vector position) {
        scheduleBlockUpdate(position.getBlockX(), position.getBlockY(), position.getBlockZ());
    }

    @Unique
    public default void scheduleFluidUpdate(org.bukkit.util.Vector position) {
        scheduleFluidUpdate(position.getBlockX(), position.getBlockY(), position.getBlockZ());
    }

}
