package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.craftbukkit.v.block.CraftBlockEntityState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

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
    @org.spongepowered.asm.mixin.Final
    private BlockEntity tileEntity;

    @Shadow
    @org.spongepowered.asm.mixin.Final
    private BlockEntity snapshot;

    /** Paper 的 {@code TileState#isSnapshot()}：快照副本与世界里的实体不是同一个对象即为快照。 */
    @Unique
    public boolean isSnapshot() {
        return this.snapshot != this.tileEntity;
    }

    @Shadow
    protected abstract BlockEntity getTileEntity();

    @Override
    public BlockEntity paperarc$getTileEntity() {
        return this.getTileEntity();
    }

    @Shadow
    protected abstract BlockEntity getTileEntityFromWorld();

    @Override
    public BlockEntity paperarc$getTileEntityFromWorld() {
        return this.getTileEntityFromWorld();
    }
}
