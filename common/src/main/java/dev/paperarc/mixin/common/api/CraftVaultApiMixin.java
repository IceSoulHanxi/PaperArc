package dev.paperarc.mixin.common.api;

import org.bukkit.block.data.type.Vault;
import org.bukkit.craftbukkit.v.block.CraftVault;
import dev.paperarc.bridge.craft.CraftBlockStateBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of the paper-api {@code Vault} additions on {@link CraftVault}:
 * trial-spawner state and ominous flag. All map to vanilla vault
 * blockstate properties, so everything delegates through the block-data
 * implementation.
 */
@Mixin(CraftVault.class)
public abstract class CraftVaultApiMixin {

    @Unique
    private org.bukkit.block.data.BlockData getBlockData() {
        return ((CraftBlockStateBridge) (Object) this).paperarc$getBlockData();
    }

    @Unique
    private void setBlockData(org.bukkit.block.data.BlockData blockData) {
        ((CraftBlockStateBridge) (Object) this).paperarc$setBlockData(blockData);
    }

    @Unique
    public Vault.State getTrialSpawnerState() {
        return ((Vault) this.getBlockData()).getTrialSpawnerState();
    }

    @Unique
    public void setTrialSpawnerState(Vault.State state) {
        Vault blockData = (Vault) this.getBlockData();
        blockData.setTrialSpawnerState(state);
        this.setBlockData(blockData);
    }

    @Unique
    public boolean isOminous() {
        return ((Vault) this.getBlockData()).isOminous();
    }

    @Unique
    public void setOminous(boolean ominous) {
        Vault blockData = (Vault) this.getBlockData();
        blockData.setOminous(ominous);
        this.setBlockData(blockData);
    }
}
