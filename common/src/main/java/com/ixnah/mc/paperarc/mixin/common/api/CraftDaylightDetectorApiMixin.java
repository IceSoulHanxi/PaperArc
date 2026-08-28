package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.DaylightDetector;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftDaylightDetector;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockStateBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds paper-api's tile-state {@code org.bukkit.block.DaylightDetector}
 * {@code isInverted()} / {@code setInverted(boolean)} to CraftBukkit's
 * {@link CraftDaylightDetector}. Delegates to the block-data view
 * (backed by Paper's {@code inverted} block state property) so validation
 * and snapshot persistence stay in vanilla/CB code paths.
 */
@Mixin(CraftDaylightDetector.class)
public abstract class CraftDaylightDetectorApiMixin {

    @Unique
    private BlockData getBlockData() {
        return (BlockData) ((CraftBlockStateBridge) (Object) this).paperarc$getBlockData();
    }

    @Unique
    private void setBlockData(BlockData data) {
        ((CraftBlockStateBridge) (Object) this).paperarc$setBlockData(data);
    }

    @Unique
    public boolean isInverted() {
        return ((DaylightDetector) this.getBlockData()).isInverted();
    }

    @Unique
    public void setInverted(boolean inverted) {
        BlockData data = this.getBlockData();
        ((DaylightDetector) data).setInverted(inverted);
        this.setBlockData(data);
    }
}
