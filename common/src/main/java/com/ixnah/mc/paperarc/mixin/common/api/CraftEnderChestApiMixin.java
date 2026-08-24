package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.block.CraftEnderChest;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockStateBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.core.BlockPos;

/**
 * Adds isBlocked missing from Arclight CraftBukkit.
 * Paper ref: patches/server/More-Chest-Block-API.patch (CraftEnderChest#isBlocked).
 */
@Mixin(CraftEnderChest.class)
public abstract class CraftEnderChestApiMixin {

    @Unique
    private net.minecraft.world.level.LevelAccessor getWorldHandle() {
        return ((CraftBlockStateBridge) (Object) this).paperarc$getWorldHandle();
    }

    @Unique
    private BlockPos getPosition() {
        return (BlockPos) ((CraftBlockStateBridge) (Object) this).paperarc$getPosition();
    }

    @Unique
    private boolean isPlaced() {
        return ((CraftBlockStateBridge) (Object) this).paperarc$isPlaced();
    }

    @Unique
    public boolean isBlocked() {
        // Same logic as EnderChestBlock's open-container check
        BlockPos abovePos = this.getPosition().above();
        return this.isPlaced() && this.getWorldHandle().getBlockState(abovePos).isRedstoneConductor(this.getWorldHandle(), abovePos);
    }
}
