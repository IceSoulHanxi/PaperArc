package dev.paperarc.mixin.common.api;

import org.bukkit.block.data.type.SculkShrieker;
import org.bukkit.craftbukkit.v.block.CraftSculkShrieker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of the paper-api {@code SculkShrieker} additions on
 * {@link CraftSculkShrieker}: {@code is/setCanSummon} and
 * {@code is/setShrieking}. Both map to vanilla blockstate properties
 * ({@code can_summon}, {@code shrieking}), so everything delegates through
 * the block-data implementation.
 */
@Mixin(CraftSculkShrieker.class)
public abstract class CraftSculkShriekerApiMixin {

    @Shadow
    public abstract org.bukkit.block.data.BlockData getBlockData();

    @Shadow
    public abstract void setBlockData(org.bukkit.block.data.BlockData blockData);

    @Unique
    public boolean isCanSummon() {
        return ((SculkShrieker) this.getBlockData()).isCanSummon();
    }

    @Unique
    public void setCanSummon(boolean canSummon) {
        SculkShrieker blockData = (SculkShrieker) this.getBlockData();
        blockData.setCanSummon(canSummon);
        this.setBlockData(blockData);
    }

    @Unique
    public boolean isShrieking() {
        return ((SculkShrieker) this.getBlockData()).isShrieking();
    }

    @Unique
    public void setShrieking(boolean shrieking) {
        SculkShrieker blockData = (SculkShrieker) this.getBlockData();
        blockData.setShrieking(shrieking);
        this.setBlockData(blockData);
    }
}
