package dev.paperarc.mixin.common.api;

import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.craftbukkit.v.block.CraftBed;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's {@code Add-missing-block-data-API} block-state additions on
 * {@link CraftBed}: {@code getPart()}, {@code setPart(Part)},
 * {@code isOccupied()} and {@code setOccupied(boolean)}.
 *
 * <p>The TileState host delegates all four methods to its block-data snapshot,
 * exactly as Paper does for part; occupancy is backed by the vanilla
 * {@code OCCUPIED} blockstate property via the data impl (see
 * {@link CraftBedBlockDataApiMixin}).</p>
 */
@Mixin(CraftBed.class)
public abstract class CraftBedApiMixin {

    @Shadow
    public abstract BlockData getBlockData();

    @Shadow
    public abstract void setBlockData(BlockData blockData);

    @Unique
    public Bed.Part getPart() {
        return ((Bed) this.getBlockData()).getPart();
    }

    @Unique
    public void setPart(Bed.Part part) {
        Bed data = (Bed) this.getBlockData();
        data.setPart(part);
        this.setBlockData(data);
    }

    @Unique
    public boolean isOccupied() {
        return ((Bed) this.getBlockData()).isOccupied();
    }

    @Unique
    public void setOccupied(boolean occupied) {
        Bed data = (Bed) this.getBlockData();
        data.setOccupied(occupied);
        this.setBlockData(data);
    }
}
