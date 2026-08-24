package dev.paperarc.mixin.common.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Chest;
import org.bukkit.craftbukkit.v.block.CraftBlockState;
import org.bukkit.craftbukkit.v.block.CraftChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Port of Paper's More-Chest-Block-API.patch additions on
 * {@link CraftChest}: {@code Chest#isBlocked()} and
 * {@code Chest#setType(Chest.Type)}.
 *
 * Runtime shape note: worldHandle/position/data/blockData accessors are
 * declared on the ancestor CraftBlockState, and @Shadow cannot resolve
 * inherited members on Arclight's runtime-generated CB classes, so they are
 * reached through cached reflection.
 */
@Mixin(CraftChest.class)
public abstract class CraftChestApiMixin {

    @Unique
    private static volatile boolean paperarc$resolved;

    @Unique
    private static Field paperarc$dataField;

    @Unique
    private static Method paperarc$getWorldHandle;

    @Unique
    private static Method paperarc$getPosition;

    @Unique
    private static Method paperarc$getBlockData;

    @Unique
    private static Method paperarc$setBlockData;

    @Unique
    private static void paperarc$resolve() {
        if (paperarc$resolved) {
            return;
        }
        synchronized (CraftChestApiMixin.class) {
            if (paperarc$resolved) {
                return;
            }
            try {
                Class<?> state = CraftBlockState.class;
                Field data = state.getDeclaredField("data");
                data.setAccessible(true);
                paperarc$dataField = data;
                paperarc$getWorldHandle = state.getMethod("getWorldHandle");
                paperarc$getPosition = state.getMethod("getPosition");
                paperarc$getBlockData = state.getMethod("getBlockData");
                paperarc$setBlockData = state.getMethod("setBlockData", BlockData.class);
                paperarc$resolved = true;
            } catch (Exception e) {
                throw new IllegalStateException("paperarc: cannot bind CraftBlockState internals", e);
            }
        }
    }

    @Unique
    private LevelAccessor paperarc$worldHandle() {
        paperarc$resolve();
        try {
            return (LevelAccessor) paperarc$getWorldHandle.invoke(this);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Unique
    private BlockPos paperarc$position() {
        paperarc$resolve();
        try {
            return (BlockPos) paperarc$getPosition.invoke(this);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Unique
    private BlockState paperarc$data() {
        paperarc$resolve();
        try {
            return (BlockState) paperarc$dataField.get(this);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Unique
    private void paperarc$setBlockData(BlockData blockData) {
        paperarc$resolve();
        try {
            paperarc$setBlockData.invoke(this, blockData);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Unique
    public boolean isBlocked() {
        // Mimics vanilla logic in ChestBlock/DoubleBlockCombiner when opening the container, as in Paper
        if (this.paperarc$isUnplaced()) {
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

    @Unique
    private BlockData paperarc$getBlockData() {
        paperarc$resolve();
        try {
            return (BlockData) paperarc$getBlockData.invoke(this);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Inverse of Paper's {@code isPlaced()} helper; CraftBukkit tracks
     * placement via the nullable world/world-handle fields.
     */
    @Unique
    private boolean paperarc$isUnplaced() {
        return this.paperarc$worldHandle() == null; // unplaced states have no world handle
    }
}
