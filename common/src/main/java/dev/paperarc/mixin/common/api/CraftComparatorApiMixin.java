package dev.paperarc.mixin.common.api;

import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Comparator;
import org.bukkit.craftbukkit.v.block.CraftComparator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds paper-api's tile-state {@code org.bukkit.block.Comparator}
 * {@code getMode()} / {@code setMode(Mode)} to CraftBukkit's
 * {@link CraftComparator}. Delegates to the block-data view so property
 * validation and snapshot persistence stay in vanilla/CB code paths.
 */
@Mixin(CraftComparator.class)
public abstract class CraftComparatorApiMixin {

    @Shadow
    public abstract BlockData getBlockData();

    @Shadow
    public abstract void setBlockData(BlockData data);

    @Unique
    public Comparator.Mode getMode() {
        return ((Comparator) this.getBlockData()).getMode();
    }

    @Unique
    public void setMode(Comparator.Mode mode) {
        BlockData data = this.getBlockData();
        ((Comparator) data).setMode(mode);
        this.setBlockData(data);
    }
}
