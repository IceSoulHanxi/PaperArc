package dev.paperarc.mixin.common.api;

import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.DecoratedPot;
import org.bukkit.craftbukkit.v.block.CraftDecoratedPot;
import dev.paperarc.bridge.craft.CraftBlockStateBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds paper-api's tile-state {@code org.bukkit.block.DecoratedPot}
 * {@code isCracked()} / {@code setCracked(boolean)} to CraftBukkit's
 * {@link CraftDecoratedPot}. Delegates to the block-data view
 * (backed by Paper's {@code cracked} block state property from
 * Add-missing-block-data-API) so validation and snapshot persistence
 * stay in vanilla/CB code paths.
 */
@Mixin(CraftDecoratedPot.class)
public abstract class CraftDecoratedPotApiMixin {

    @Unique
    private BlockData getBlockData() {
        return (BlockData) ((CraftBlockStateBridge) (Object) this).paperarc$getBlockData();
    }

    @Unique
    private void setBlockData(BlockData data) {
        ((CraftBlockStateBridge) (Object) this).paperarc$setBlockData(data);
    }

    @Unique
    public boolean isCracked() {
        return ((DecoratedPot) this.getBlockData()).isCracked();
    }

    @Unique
    public void setCracked(boolean cracked) {
        BlockData data = this.getBlockData();
        ((DecoratedPot) data).setCracked(cracked);
        this.setBlockData(data);
    }
}
