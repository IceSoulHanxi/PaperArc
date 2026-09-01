package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bell;
import org.bukkit.craftbukkit.v.block.CraftBell;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockStateBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds {@code Bell#getAttachment()} / {@code Bell#setAttachment(...)} from
 * paper-api's tile-state {@code org.bukkit.block.Bell} to CraftBukkit's
 * {@link CraftBell}. Delegates to the block-data view
 * ({@code org.bukkit.craftbukkit.v.block.impl.CraftBell}) so property
 * validation and snapshot persistence stay in vanilla/CB code paths.
 */
@Mixin(CraftBell.class)
public abstract class CraftBellApiMixin {

    @Unique
    private BlockData getBlockData() {
        return (BlockData) ((CraftBlockStateBridge) (Object) this).paperarc$getBlockData();
    }

    @Unique
    private void setBlockData(BlockData data) {
        ((CraftBlockStateBridge) (Object) this).paperarc$setBlockData(data);
    }

    @Unique
    public Bell.Attachment getAttachment() {
        return ((Bell) this.getBlockData()).getAttachment();
    }

    @Unique
    public void setAttachment(Bell.Attachment attachment) {
        BlockData data = this.getBlockData();
        ((Bell) data).setAttachment(attachment);
        this.setBlockData(data);
    }
}
