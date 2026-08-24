package dev.paperarc.mixin.common.api;

import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Crafter;
import org.bukkit.craftbukkit.v.block.CraftCrafter;
import dev.paperarc.bridge.craft.CraftBlockStateBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Implements the {@code org.bukkit.block.data.type.Crafter} API
 * ({@code getOrientation()}/{@code setOrientation(Orientation)},
 * {@code isCrafting()}/{@code setCrafting(boolean)}) on the TileState host by
 * delegating to its block-data snapshot. The concrete data impl
 * ({@code v.block.impl.CraftCrafter}) already implements these over the
 * vanilla crafter blockstate properties.
 */
@Mixin(CraftCrafter.class)
public abstract class CraftCrafterApiMixin {

    @Unique
    private BlockData getBlockData() {
        return (BlockData) ((CraftBlockStateBridge) (Object) this).paperarc$getBlockData();
    }

    @Unique
    private void setBlockData(BlockData blockData) {
        ((CraftBlockStateBridge) (Object) this).paperarc$setBlockData(blockData);
    }

    @Unique
    public Crafter.Orientation getOrientation() {
        return ((Crafter) this.getBlockData()).getOrientation();
    }

    @Unique
    public void setOrientation(Crafter.Orientation orientation) {
        Crafter data = (Crafter) this.getBlockData();
        data.setOrientation(orientation);
        this.setBlockData(data);
    }

    @Unique
    public boolean isCrafting() {
        return ((Crafter) this.getBlockData()).isCrafting();
    }

    @Unique
    public void setCrafting(boolean crafting) {
        Crafter data = (Crafter) this.getBlockData();
        data.setCrafting(crafting);
        this.setBlockData(data);
    }
}
