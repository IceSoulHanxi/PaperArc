package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.block.data.BlockData;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code BlockPhysicsEvent#getChangedBlockData()}。
 *
 * <p>运行时已经把 {@code BlockData} 原样存在 {@code changed} 字段里
 * （旧 API 只暴露了 {@code getChangedType()} 这个 {@code Material} 视图），
 * {@code @Shadow} 该字段返回即可，不需要触发点配合。
 */
@Mixin(BlockPhysicsEvent.class)
public abstract class BlockPhysicsEventApiMixin {

    @Shadow(remap = false)
    @Final
    private BlockData changed;

    @Unique
    public BlockData getChangedBlockData() {
        return this.changed;
    }
}
