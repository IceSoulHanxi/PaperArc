package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.Chunk} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.Chunk", remap = false)
public interface ChunkIfaceMixin {

    @Unique
    public abstract java.util.Collection<org.bukkit.block.BlockState> getTileEntities(java.util.function.Predicate<org.bukkit.block.Block> p0, boolean p1);

    @Unique
    public abstract org.bukkit.block.BlockState[] getTileEntities(boolean p0);

}