package dev.paperarc.mixin.common.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Chest;
import org.bukkit.craftbukkit.v.block.CraftChest;
import dev.paperarc.bridge.craft.CraftBlockStateBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's More-Chest-Block-API.patch additions on
 * {@link CraftChest}: {@code Chest#isBlocked()} and
 * {@code Chest#setType(Chest.Type)}.
 */
@Mixin(CraftChest.class)
public abstract class CraftChestApiMixin {

    @Shadow
    protected BlockState data;

    @Unique
    private LevelAccessor getWorldHandle() {
        return (LevelAccessor) ((CraftBlockStateBridge) (Object) this).paperarc$getWorldHandle();
    }

    @Unique
    private BlockPos getPosition() {
        return (BlockPos) ((CraftBlockStateBridge) (Object) this).paperarc$getPosition();
    }

    @Unique
    private BlockData getBlockData() {
        return (BlockData) ((CraftBlockStateBridge) (Object) this).paperarc$getBlockData();
    }

    @Unique
    private void setBlockData(BlockData blockData) {
        ((CraftBlockStateBridge) (Object) this).paperarc$setBlockData(blockData);
    }

    @Unique
    public boolean isBlocked() {
        // Mimics vanilla logic in ChestBlock/DoubleBlockCombiner when opening the container, as in Paper
        if (this.paperarc$isUnplaced()) {
            return false;
        }
        LevelAccessor world = this.getWorldHandle();
        if (ChestBlock.isChestBlockedAt(world, this.getPosition())) {
            return true;
        }
        if (ChestBlock.getBlockType(this.data) == DoubleBlockCombiner.BlockType.SINGLE) {
            return false;
        }
        Direction direction = ChestBlock.getConnectedDirection(this.data);
        BlockPos neighbourBlockPos = this.getPosition().relative(direction);
        // getBlockStateIfLoaded is not present in these mappings: emulate via chunk-load check
        BlockState neighbourBlockState = world.hasChunkAt(neighbourBlockPos) ? world.getBlockState(neighbourBlockPos) : null;
        return neighbourBlockState != null
            && neighbourBlockState.is(this.data.getBlock())
            && ChestBlock.getBlockType(neighbourBlockState) != DoubleBlockCombiner.BlockType.SINGLE
            && ChestBlock.getConnectedDirection(neighbourBlockState) == direction.getOpposite()
            && ChestBlock.isChestBlockedAt(world, neighbourBlockPos);
    }

    @Unique
    public void setType(Chest.Type type) {
        Chest blockData = (Chest) this.getBlockData();
        blockData.setType(type);
        this.setBlockData(blockData);
    }

    /**
     * Inverse of Paper's {@code isPlaced()} helper; CraftBukkit tracks
     * placement via the nullable world/world-handle fields.
     */
    @Unique
    private boolean paperarc$isUnplaced() {
        return this.getWorldHandle() == null; // unplaced states have no world handle
    }
}
