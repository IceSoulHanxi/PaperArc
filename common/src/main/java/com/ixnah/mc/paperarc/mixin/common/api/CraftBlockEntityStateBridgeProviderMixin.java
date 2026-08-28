package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockEntityState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Merges {@link CraftBlockEntityStateBridge} onto {@code CraftBlockEntityState}.
 * The generic accessors erase to BlockEntity; consumers narrow the result.
 */
@Mixin(CraftBlockEntityState.class)
public abstract class CraftBlockEntityStateBridgeProviderMixin implements CraftBlockEntityStateBridge {

    @Shadow
    protected abstract BlockEntity getSnapshot();

    @Override
    public BlockEntity paperarc$getSnapshot() {
        return this.getSnapshot();
    }

    @Shadow
    protected abstract BlockEntity getTileEntityFromWorld();

    @Override
    public BlockEntity paperarc$getTileEntityFromWorld() {
        return this.getTileEntityFromWorld();
    }
}
