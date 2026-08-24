package com.ixnah.mc.paperarc.mixin.common.api;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v.CraftChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's CraftChunk light-optional snapshot and tile-entity APIs.
 *
 * The 4-arg getChunkSnapshot builds on the existing CraftBukkit snapshot code
 * (delegated via the 3-arg overload); includeLightData=false strips the light
 * arrays reflectively afterwards so later accessors fail exactly like Paper
 * ("ChunkSnapshot created without light data"). Tile entities are enumerated
 * through ChunkAccess#getBlockEntitiesPos (the map field itself is protected).
 */
@Mixin(CraftChunk.class)
public abstract class CraftChunkApiMixin {

    @Unique
    private static volatile Field PAPERARC$SKYLIGHT_FIELD;

    @Unique
    private static volatile Field PAPERARC$EMITLIGHT_FIELD;

    @Shadow
    @Final
    private ServerLevel worldServer;

    @Shadow
    @Final
    private int x;

    @Shadow
    @Final
    private int z;

    @Shadow
    public abstract boolean isLoaded();

    @Shadow
    public abstract ChunkAccess getHandle(ChunkStatus status);

    @Shadow
    public abstract World getWorld();

    @Shadow
    public abstract ChunkSnapshot getChunkSnapshot(boolean includeMaxBlockY, boolean includeBiome, boolean includeBiomeTempRain);

    @Unique
    private static Field paperarc$snapshotField(String name) throws NoSuchFieldException {
        Field cached = "skylight".equals(name) ? PAPERARC$SKYLIGHT_FIELD : PAPERARC$EMITLIGHT_FIELD;
        if (cached == null) {
            synchronized (CraftChunkApiMixin.class) {
                Field resolved = org.bukkit.craftbukkit.v.CraftChunkSnapshot.class.getDeclaredField(name);
                resolved.setAccessible(true);
                if ("skylight".equals(name)) {
                    PAPERARC$SKYLIGHT_FIELD = resolved;
                } else {
                    PAPERARC$EMITLIGHT_FIELD = resolved;
                }
                cached = resolved;
            }
        }
        return cached;
    }

    @Unique
    public ChunkSnapshot getChunkSnapshot(boolean includeMaxBlockY, boolean includeBiome, boolean includeBiomeTempRain, boolean includeLightData) {
        ChunkSnapshot snapshot = getChunkSnapshot(includeMaxBlockY, includeBiome, includeBiomeTempRain);
        if (!includeLightData && snapshot instanceof org.bukkit.craftbukkit.v.CraftChunkSnapshot craftSnapshot) {
            try {
                paperarc$snapshotField("skylight").set(craftSnapshot, null);
                paperarc$snapshotField("emitlight").set(craftSnapshot, null);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Could not strip light data from chunk snapshot", e);
            }
        }
        return snapshot;
    }

    @Unique
    public List<BlockState> getTileEntities(java.util.function.Predicate<? super Block> blockPredicate, boolean useSnapshot) {
        Preconditions.checkNotNull(blockPredicate, "blockPredicate");
        if (!this.isLoaded()) {
            this.getWorld().getChunkAt(this.x, this.z); // Transient load for this tick
        }
        ChunkAccess chunk = this.getHandle(ChunkStatus.FULL);

        List<BlockState> entities = new ArrayList<>();

        for (BlockPos position : chunk.getBlockEntitiesPos()) {
            Block block = this.getWorld().getBlockAt(position.getX(), position.getY(), position.getZ());
            if (blockPredicate.test(block)) {
                entities.add(block.getState(useSnapshot));
            }
        }

        return entities;
    }

    @Unique
    public BlockState[] getTileEntities(boolean useSnapshot) {
        return getTileEntities(block -> true, useSnapshot).toArray(new BlockState[0]);
    }
}
