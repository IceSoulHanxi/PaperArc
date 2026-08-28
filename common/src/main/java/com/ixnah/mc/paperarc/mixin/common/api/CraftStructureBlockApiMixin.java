package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.StructureBlock;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftStructureBlock;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockStateBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds paper-api's tile-state {@code org.bukkit.block.StructureBlock}
 * {@code getMode()} / {@code setMode(Mode)} to CraftBukkit's
 * {@link CraftStructureBlock}. Delegates to the block-data view (vanilla
 * {@code mode} block state property) so validation and snapshot
 * persistence stay in vanilla/CB code paths.
 */
@Mixin(CraftStructureBlock.class)
public abstract class CraftStructureBlockApiMixin {

    @Unique
    private BlockData getBlockData() {
        return (BlockData) ((CraftBlockStateBridge) (Object) this).paperarc$getBlockData();
    }

    @Unique
    private void setBlockData(BlockData data) {
        ((CraftBlockStateBridge) (Object) this).paperarc$setBlockData(data);
    }

    @Unique
    public StructureBlock.Mode getMode() {
        return ((StructureBlock) this.getBlockData()).getMode();
    }

    @Unique
    public void setMode(StructureBlock.Mode mode) {
        BlockData data = this.getBlockData();
        ((StructureBlock) data).setMode(mode);
        this.setBlockData(data);
    }
}
