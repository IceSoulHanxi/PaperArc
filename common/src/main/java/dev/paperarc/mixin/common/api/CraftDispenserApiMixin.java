package dev.paperarc.mixin.common.api;

import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.craftbukkit.v.block.CraftDispenser;
import dev.paperarc.bridge.craft.CraftBlockStateBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds paper-api's tile-state {@code org.bukkit.block.Dispenser}
 * {@code isTriggered()} / {@code setTriggered(boolean)} to CraftBukkit's
 * {@link CraftDispenser}. Delegates to the block-data view (vanilla
 * {@code triggered} block state property) so validation and snapshot
 * persistence stay in vanilla/CB code paths.
 */
@Mixin(CraftDispenser.class)
public abstract class CraftDispenserApiMixin {

    @Unique
    private BlockData getBlockData() {
        return (BlockData) ((CraftBlockStateBridge) (Object) this).paperarc$getBlockData();
    }

    @Unique
    private void setBlockData(BlockData data) {
        ((CraftBlockStateBridge) (Object) this).paperarc$setBlockData(data);
    }

    @Unique
    public boolean isTriggered() {
        return ((Dispenser) this.getBlockData()).isTriggered();
    }

    @Unique
    public void setTriggered(boolean triggered) {
        BlockData data = this.getBlockData();
        ((Dispenser) data).setTriggered(triggered);
        this.setBlockData(data);
    }
}
