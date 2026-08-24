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
import dev.paperarc.bridge.craft.CraftEntityBridge;
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
 * public setter; it is opened up via the access widener.</p>
 */
@Mixin(CraftFallingBlock.class)
public abstract class CraftFallingBlockApiMixin {

    
    // Inherited from CraftEntity (protected); resolved through the target hierarchy.
    @Unique
    private void update() {
        ((CraftEntityBridge) (Object) this).paperarc$update();
    }

    @Unique
    private static final String PAPERARC$AUTO_EXPIRE_KEY = "autoExpire";

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
        FallingBlockEntity handle = getHandle();
        BlockState oldState = handle.blockState;
        BlockState newState = ((CraftBlockData) blockData).getState();
        handle.blockState = newState;
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
