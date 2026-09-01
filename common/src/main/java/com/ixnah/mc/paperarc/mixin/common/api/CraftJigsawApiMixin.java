package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Jigsaw;
import org.bukkit.craftbukkit.v.block.CraftJigsaw;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockStateBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds paper-api's tile-state {@code org.bukkit.block.Jigsaw}
 * {@code getOrientation()} / {@code setOrientation(Orientation)} to
 * CraftBukkit's {@link CraftJigsaw}. Delegates to the block-data view
 * (vanilla {@code orientation} block state property) so validation and
 * snapshot persistence stay in vanilla/CB code paths.
 */
@Mixin(CraftJigsaw.class)
public abstract class CraftJigsawApiMixin {

    @Unique
    private BlockData getBlockData() {
        return (BlockData) ((CraftBlockStateBridge) (Object) this).paperarc$getBlockData();
    }

    @Unique
    private void setBlockData(BlockData data) {
        ((CraftBlockStateBridge) (Object) this).paperarc$setBlockData(data);
    }

    @Unique
    public Jigsaw.Orientation getOrientation() {
        return ((Jigsaw) this.getBlockData()).getOrientation();
    }

    @Unique
    public void setOrientation(Jigsaw.Orientation orientation) {
        BlockData data = this.getBlockData();
        ((Jigsaw) data).setOrientation(orientation);
        this.setBlockData(data);
    }
}
