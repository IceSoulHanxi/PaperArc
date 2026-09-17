package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.TileState}.
 * 实现体在 {@code api.CraftBlockEntityStateBridgeProviderMixin}（快照副本 != 世界里的实体）。
 */
@Mixin(targets = "org.bukkit.block.TileState", remap = false)
public interface TileStateIfaceMixin {

    @Unique
    public abstract boolean isSnapshot();
}
