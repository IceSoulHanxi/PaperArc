package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.CraftBlockStateBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Merges {@link CraftBlockStateBridge} onto {@code CraftBlockState} so that
 * child-state api mixins can reach the state internals via a plain virtual
 * call (duck typing) instead of reflection; @Shadow is safe here because
 * every member below is declared in this exact target class.
 */
@Mixin(CraftBlockState.class)
public abstract class CraftBlockStateBridgeProviderMixin implements CraftBlockStateBridge {

    @Shadow
    protected BlockState data;

    @Shadow
    public abstract LevelAccessor getWorldHandle();

    @Override
    public LevelAccessor paperarc$getWorldHandle() {
        return this.getWorldHandle();
    }

    @Shadow
    public abstract World getWorld();

    @Override
    public World paperarc$getWorld() {
        return this.getWorld();
    }

    @Shadow
    public abstract BlockPos getPosition();

    @Override
    public BlockPos paperarc$getPosition() {
        return this.getPosition();
    }

    @Shadow
    public abstract boolean isPlaced();

    @Override
    public boolean paperarc$isPlaced() {
        return this.isPlaced();
    }

    @Shadow
    public abstract BlockData getBlockData();

    @Override
    public BlockData paperarc$getBlockData() {
        return this.getBlockData();
    }

    @Shadow
    public abstract void setBlockData(BlockData blockData);

    @Override
    public void paperarc$setBlockData(BlockData blockData) {
        this.setBlockData(blockData);
    }

    @Override
    public BlockState paperarc$data() {
        return this.data;
    }
}
