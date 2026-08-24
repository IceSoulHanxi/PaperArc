package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.TrialSpawner;
import org.bukkit.craftbukkit.v.block.CraftTrialSpawner;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockStateBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds paper-api's tile-state {@code org.bukkit.block.TrialSpawner}
 * {@code getTrialSpawnerState()} / {@code setTrialSpawnerState(State)} to
 * CraftBukkit's {@link CraftTrialSpawner}. Delegates to the block-data view
 * (vanilla {@code trial_spawner_state} block state property) so validation
 * and snapshot persistence stay in vanilla/CB code paths.
 */
@Mixin(CraftTrialSpawner.class)
public abstract class CraftTrialSpawnerApiMixin {

    @Unique
    private BlockData getBlockData() {
        return (BlockData) ((CraftBlockStateBridge) (Object) this).paperarc$getBlockData();
    }

    @Unique
    private void setBlockData(BlockData data) {
        ((CraftBlockStateBridge) (Object) this).paperarc$setBlockData(data);
    }

    @Unique
    public TrialSpawner.State getTrialSpawnerState() {
        return ((TrialSpawner) this.getBlockData()).getTrialSpawnerState();
    }

    @Unique
    public void setTrialSpawnerState(TrialSpawner.State state) {
        BlockData data = this.getBlockData();
        ((TrialSpawner) data).setTrialSpawnerState(state);
        this.setBlockData(data);
    }
}
