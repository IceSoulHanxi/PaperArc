package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.Chunk;
import java.util.Collection;
import java.util.function.Predicate;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.generator.structure.Structure;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.plugin.Plugin;
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

    @Unique
    public default long getChunkKey() {
        Chunk self = (Chunk) this;
        return com.ixnah.mc.paperarc.bridge.api.PaperarcBlockKeys.packChunk(self.getX(), self.getZ());
    }
}
