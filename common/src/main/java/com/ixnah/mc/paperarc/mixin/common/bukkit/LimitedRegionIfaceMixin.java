package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.generator.LimitedRegion;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.RegionAccessor;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;
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

    @Unique
    public default void setBlockData(Vector vector, BlockData data) {
        LimitedRegion self = (LimitedRegion) this;
        self.setBlockData(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ(), data);
    }

    @Unique
    public default void setBlockState(Vector location, BlockState state) {
        LimitedRegion self = (LimitedRegion) this;
        self.setBlockState(location.getBlockX(), location.getBlockY(), location.getBlockZ(), state);
    }

    @Unique
    public default BlockState getBlockState(Vector location) {
        LimitedRegion self = (LimitedRegion) this;
        return self.getBlockState(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Unique
    public default void scheduleBlockUpdate(Vector location) {
        LimitedRegion self = (LimitedRegion) this;
        self.scheduleBlockUpdate(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Unique
    public default void scheduleFluidUpdate(Vector location) {
        LimitedRegion self = (LimitedRegion) this;
        self.scheduleFluidUpdate(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Unique
    public default BlockData getBlockData(Vector vector) {
        LimitedRegion self = (LimitedRegion) this;
        return self.getBlockData(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    @Unique
    public default int getCenterBlockX() {
        LimitedRegion self = (LimitedRegion) this;
        return self.getCenterChunkX() << 4;
    }

    @Unique
    public default int getCenterBlockZ() {
        LimitedRegion self = (LimitedRegion) this;
        return self.getCenterChunkZ() << 4;
    }
}
