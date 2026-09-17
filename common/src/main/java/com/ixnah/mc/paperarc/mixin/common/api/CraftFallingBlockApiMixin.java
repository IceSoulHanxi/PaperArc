package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import com.ixnah.mc.paperarc.bridge.FallingBlockEntityBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v.block.CraftBlockStates;
import org.bukkit.craftbukkit.v.entity.CraftFallingBlock;
import com.ixnah.mc.paperarc.bridge.craft.CraftEntityBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Expand-FallingBlock-API to {@link CraftFallingBlock}.
 *
 * <p>The {@code autoExpire} flag lives in an NMS field added by Paper's server
 * patch ({@code FallingBlockEntity.autoExpire}); it is injected into the NMS
 * class by {@code FallingBlockEntityFieldsMixin} and reached through
 * {@link com.ixnah.mc.paperarc.bridge.FallingBlockEntityBridge} (default
 * {@code true}, matching Paper's default).</p>
 *
 * <p>{@code FallingBlockEntity.blockState} is private in vanilla 1.21.1 with no
 * public setter, so {@link #setBlockData(BlockState)} writes it directly (the
 * field is public in this NMS build; no reflection).</p>
 */
@Mixin(CraftFallingBlock.class)
public abstract class CraftFallingBlockApiMixin {

    @Shadow
    public abstract FallingBlockEntity getHandle();

    // Inherited from CraftEntity (protected); resolved through the target hierarchy.
    @Unique
    private void update() {
        ((CraftEntityBridge) (Object) this).paperarc$update();
    }

    @Unique
    public boolean doesAutoExpire() {
        return ((com.ixnah.mc.paperarc.bridge.FallingBlockEntityBridge) getHandle()).paper$autoExpire();
    }

    @Unique
    public void shouldAutoExpire(boolean autoExpires) {
        ((com.ixnah.mc.paperarc.bridge.FallingBlockEntityBridge) getHandle()).paper$setAutoExpire(autoExpires);
    }

    @Unique
    public org.bukkit.block.BlockState getBlockState() {
        return CraftBlockStates.getBlockState(getHandle().getBlockState(), getHandle().blockData);
    }

    @Unique
    public void setBlockData(final BlockData blockData) {
        Preconditions.checkArgument(blockData != null, "blockData");
        BlockState oldState = getHandle().blockState;
        BlockState newState = ((CraftBlockData) blockData).getState();
        getHandle().blockState = newState;
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
