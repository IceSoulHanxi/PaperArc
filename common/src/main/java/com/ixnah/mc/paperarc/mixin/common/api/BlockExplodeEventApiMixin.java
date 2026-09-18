package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockExplodeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * paper 的 {@code BlockExplodeEvent#getExplodedBlockState()}。
 *
 * <p>事件在 {@code Explosion#finalizeExplosion} 里、把方块打掉**之前**构造，
 * 所以构造器里取一次 {@code getBlock().getState()} 就是 paper 传进来的那个快照。
 */
@Mixin(BlockExplodeEvent.class)
public abstract class BlockExplodeEventApiMixin {

    @Unique
    private BlockState paperarc$explodedBlockState;

    @Inject(method = "<init>(Lorg/bukkit/block/Block;Ljava/util/List;F)V", at = @At("RETURN"), remap = false)
    private void paperarc$snapshotExplodedState(Block what, List<Block> blocks, float yield, CallbackInfo ci) {
        this.paperarc$explodedBlockState = what == null ? null : what.getState();
    }

    @Unique
    public BlockState getExplodedBlockState() {
        return this.paperarc$explodedBlockState;
    }
}
