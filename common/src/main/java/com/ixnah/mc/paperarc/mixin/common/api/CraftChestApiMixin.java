package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.CraftBlockStateBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Chest;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's More-Chest-Block-API.patch additions on
 * {@link CraftChest}: {@code Chest#isBlocked()} and
 * {@code Chest#setType(Chest.Type)}.
 *
 * <p>State internals (worldHandle/position/data/blockData) live on the
 * ancestor CraftBlockState and cannot be @Shadow'ed from a child-target mixin
 * against Arclight's runtime-generated CB classes, so everything goes through
 * the {@link CraftBlockStateBridge} duck interface.</p>
 */
@Mixin(CraftChest.class)
public abstract class CraftChestApiMixin {

    @Unique
    private LevelAccessor paperarc$worldHandle() {
        return ((CraftBlockStateBridge) (Object) this).paperarc$getWorldHandle();
    }

    @Unique
    private BlockPos paperarc$position() {
        return ((CraftBlockStateBridge) (Object) this).paperarc$getPosition();
    }

    @Unique
    private BlockState paperarc$data() {
        return ((CraftBlockStateBridge) (Object) this).paperarc$data();
    }

    @Unique
    private BlockData paperarc$getBlockData() {
        return ((CraftBlockStateBridge) (Object) this).paperarc$getBlockData();
    }

    @Unique
    private void paperarc$setBlockData(BlockData blockData) {
        ((CraftBlockStateBridge) (Object) this).paperarc$setBlockData(blockData);
    }

    @Unique
    public boolean isBlocked() {
        // Mimics vanilla logic in ChestBlock/DoubleBlockCombiner when opening the container, as in Paper
        if (!((CraftBlockStateBridge) (Object) this).paperarc$isPlaced()) {
            return false;
        }
        LevelAccessor world = this.paperarc$worldHandle();
        if (world == null) {
            return false;
        }
        BlockPos position = this.paperarc$position();
        if (ChestBlock.isChestBlockedAt(world, position)) {
            return true;
        }
        BlockState data = this.paperarc$data();
        if (ChestBlock.getBlockType(data) == DoubleBlockCombiner.BlockType.SINGLE) {
            return false;
        }
        Direction direction = ChestBlock.getConnectedDirection(data);
        BlockPos neighbourBlockPos = position.relative(direction);
        // getBlockStateIfLoaded is not present in these mappings: emulate via chunk-load check
        BlockState neighbourBlockState = world.hasChunkAt(neighbourBlockPos) ? world.getBlockState(neighbourBlockPos) : null;
        return neighbourBlockState != null
            && neighbourBlockState.is(data.getBlock())
            && ChestBlock.getBlockType(neighbourBlockState) != DoubleBlockCombiner.BlockType.SINGLE
            && ChestBlock.getConnectedDirection(neighbourBlockState) == direction.getOpposite()
            && ChestBlock.isChestBlockedAt(world, neighbourBlockPos);
    }

    @Unique
    public void setType(Chest.Type type) {
        Chest blockData = (Chest) this.paperarc$getBlockData();
        blockData.setType(type);
        this.paperarc$setBlockData(blockData);
    }
}
