package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_20_R1.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R1.generator.CraftLimitedRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

/**
 * Adds Paper's "Add more LimitedRegion API" additions to CraftLimitedRegion.
 *
 * Paper ref: patches/server/Add-more-LimitedRegion-API.patch.
 * Mapping notes vs Paper source:
 * - Paper calls getHandle().getMinecraftWorld(); mojmap equivalent is
 *   WorldGenRegion#getLevel() -> ServerLevel. ServerLevel#getWorld() is not present
 *   in the deobf runtime, so the world handle goes through PaperArcBridge#bukkitWorld.
 * - Paper calls NMS BlockState#createCraftBlockData() (Paper-added); replaced with
 *   CraftBlockData#fromData(nms).
 */
@Mixin(CraftLimitedRegion.class)
public abstract class CraftLimitedRegionApiMixin {

    @Shadow
    public abstract WorldGenLevel getHandle();

    @Shadow
    private int centerChunkX;

    @Shadow
    private int centerChunkZ;

    @Unique
    public World getWorld() {
        // Paper: reading/writing the returned Minecraft world causes a deadlock;
        // exposed anyway with a documented warning, same as upstream.
        return PaperArcBridge.bukkitWorld(this.getHandle().getLevel());
    }

    @Unique
    public int getCenterChunkX() {
        return this.centerChunkX;
    }

    @Unique
    public int getCenterChunkZ() {
        return this.centerChunkZ;
    }

    @Unique
    public void scheduleBlockUpdate(int x, int y, int z) {
        BlockPos position = new BlockPos(x, y, z);
        this.getHandle().scheduleTick(position, this.getHandle().getBlockState(position).getBlock(), 0);
    }

    @Unique
    public void scheduleFluidUpdate(int x, int y, int z) {
        BlockPos position = new BlockPos(x, y, z);
        this.getHandle().scheduleTick(position, this.getHandle().getFluidState(position).getType(), 0);
    }

    @Unique
    public void setBlockState(int x, int y, int z, BlockState state) {
        BlockPos pos = new BlockPos(x, y, z);
        if (!state.getBlockData().matches(CraftBlockData.fromData(this.getHandle().getBlockState(pos)))) {
            throw new IllegalArgumentException("BlockData does not match! Expected "
                + state.getBlockData().getAsString(false) + ", got "
                + CraftBlockData.fromData(this.getHandle().getBlockState(pos)).getAsString(false));
        }
        this.getHandle().getBlockEntity(pos)
            .load(((CraftBlockEntityState<?>) state).getSnapshotNBT());
    }
}
