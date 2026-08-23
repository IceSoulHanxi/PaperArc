package dev.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import dev.paperarc.bridge.ApiState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v.block.CraftBlockStates;
import org.bukkit.craftbukkit.v.entity.CraftFallingBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Expand-FallingBlock-API to {@link CraftFallingBlock}.
 *
 * <p>The {@code autoExpire} flag lives in an NMS field added by Paper's server
 * patch ({@code FallingBlockEntity.autoExpire}) which does not exist in this
 * Arclight-based runtime, so it is stored side-map style in {@link ApiState}
 * keyed by the NMS entity (default {@code true}, matching Paper's default).</p>
 *
 * <p>{@code FallingBlockEntity.blockState} is private in vanilla 1.21.1 with no
 * public setter, so {@link #setBlockData(BlockState)} writes it reflectively
 * (mojmap runtime name: {@code blockState}).</p>
 */
@Mixin(CraftFallingBlock.class)
public abstract class CraftFallingBlockApiMixin {

    @Shadow
    public abstract FallingBlockEntity getHandle();

    // Inherited from CraftEntity (protected); resolved through the target hierarchy.
    @Shadow
    protected abstract void update();

    @Unique
    private static final String PAPERARC$AUTO_EXPIRE_KEY = "autoExpire";

    @Unique
    private static volatile java.lang.reflect.Field PAPERARC$BLOCK_STATE_FIELD;

    @Unique
    private static java.lang.reflect.Field paperarc$blockStateField() {
        java.lang.reflect.Field f = PAPERARC$BLOCK_STATE_FIELD;
        if (f == null) {
            synchronized (CraftFallingBlockApiMixin.class) {
                if (PAPERARC$BLOCK_STATE_FIELD == null) {
                    try {
                        java.lang.reflect.Field resolved = FallingBlockEntity.class.getDeclaredField("blockState");
                        resolved.setAccessible(true);
                        PAPERARC$BLOCK_STATE_FIELD = resolved;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("NMS FallingBlockEntity.blockState field not found", e);
                    }
                    f = PAPERARC$BLOCK_STATE_FIELD;
                }
            }
        }
        return f;
    }

    @Unique
    private void paperarc$setNmsBlockState(BlockState newState) {
        try {
            paperarc$blockStateField().set(getHandle(), newState);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to set NMS FallingBlockEntity.blockState", e);
        }
    }

    @Unique
    public boolean doesAutoExpire() {
        return ApiState.get(getHandle(), PAPERARC$AUTO_EXPIRE_KEY, Boolean.TRUE);
    }

    @Unique
    public void shouldAutoExpire(boolean autoExpires) {
        ApiState.put(getHandle(), PAPERARC$AUTO_EXPIRE_KEY, autoExpires);
    }

    @Unique
    public org.bukkit.block.BlockState getBlockState() {
        return CraftBlockStates.getBlockState(getHandle().getBlockState(), getHandle().blockData);
    }

    @Unique
    public void setBlockData(final BlockData blockData) {
        Preconditions.checkArgument(blockData != null, "blockData");
        BlockState oldState;
        try {
            oldState = (BlockState) paperarc$blockStateField().get(getHandle());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to read NMS FallingBlockEntity.blockState", e);
        }
        BlockState newState = ((CraftBlockData) blockData).getState();
        paperarc$setNmsBlockState(newState);
        this.getHandle().blockData = null;

        if (oldState != newState) {
            this.update();
        }
    }

    @Unique
    public void setBlockState(final org.bukkit.block.BlockState blockState) {
        Preconditions.checkArgument(blockState != null, "blockState");
        // Calls #update if needed; the block data compound tag is not synced with the client
        // and hence can be mutated after the sync with clients. The call also clears any
        // potential old block data.
        this.setBlockData(blockState.getBlockData());
        if (blockState instanceof CraftBlockEntityState<?> tileEntity) {
            CompoundTag snapshot = tileEntity.getSnapshotNBT();
            this.getHandle().blockData = snapshot;
        }
    }
}
