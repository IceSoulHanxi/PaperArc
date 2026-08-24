package com.ixnah.mc.paperarc.bridge.craft;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

/**
 * Duck-typing bridge merged onto {@code CraftBlockState} by its provider
 * mixin. Child-class api mixins (whose own targets only inherit these
 * members) cast to this interface instead of using reflection.
 */
public interface CraftBlockStateBridge {

    LevelAccessor paperarc$getWorldHandle();

    World paperarc$getWorld();

    BlockPos paperarc$getPosition();

    boolean paperarc$isPlaced();

    BlockData paperarc$getBlockData();

    void paperarc$setBlockData(BlockData blockData);

    BlockState paperarc$data();
}
