package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.Chunk}.
 * Adds paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).
 */
@Mixin(targets = "org.bukkit.Chunk", remap = false)
public interface ChunkIfaceMixin {

    @Unique
    public abstract org.bukkit.ChunkSnapshot getChunkSnapshot(boolean p0, boolean p1, boolean p2, boolean p3);

    @Unique
    public abstract java.util.Collection getTileEntities(java.util.function.Predicate p0, boolean p1);

    @Unique
    public abstract org.bukkit.block.BlockState[] getTileEntities(boolean p0);
}
