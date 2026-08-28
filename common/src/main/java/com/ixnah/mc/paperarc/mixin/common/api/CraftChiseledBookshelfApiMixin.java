package com.ixnah.mc.paperarc.mixin.common.api;

import java.util.Set;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.ChiseledBookshelf;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftChiseledBookshelf;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockStateBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Implements the {@code org.bukkit.block.data.type.ChiseledBookshelf} API on
 * the TileState host by delegating to its block-data snapshot, mirroring how
 * Paper wires TileState/data-type method pairs. The concrete data impl
 * ({@code v.block.impl.CraftChiseledBookShelf}) already implements these over
 * the vanilla {@code SLOT_OCCUPIED_PROPERTIES} blockstate properties.
 */
@Mixin(CraftChiseledBookshelf.class)
public abstract class CraftChiseledBookshelfApiMixin {

    @Unique
    private BlockData getBlockData() {
        return (BlockData) ((CraftBlockStateBridge) (Object) this).paperarc$getBlockData();
    }

    @Unique
    private void setBlockData(BlockData blockData) {
        ((CraftBlockStateBridge) (Object) this).paperarc$setBlockData(blockData);
    }

    @Unique
    public int getMaximumOccupiedSlots() {
        return ((ChiseledBookshelf) this.getBlockData()).getMaximumOccupiedSlots();
    }

    @Unique
    public Set<Integer> getOccupiedSlots() {
        return ((ChiseledBookshelf) this.getBlockData()).getOccupiedSlots();
    }

    @Unique
    public boolean isSlotOccupied(int slot) {
        return ((ChiseledBookshelf) this.getBlockData()).isSlotOccupied(slot);
    }

    @Unique
    public void setSlotOccupied(int slot, boolean occupied) {
        ChiseledBookshelf data = (ChiseledBookshelf) this.getBlockData();
        data.setSlotOccupied(slot, occupied);
        this.setBlockData(data);
    }
}
