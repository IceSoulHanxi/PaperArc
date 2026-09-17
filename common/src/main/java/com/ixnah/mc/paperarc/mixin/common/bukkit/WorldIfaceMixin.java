package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.World} (generated, trimmed for 1.20.1).
 * Adds 21 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 *
 * <p>A4-3 父接口差集：补上 paper 声明的父接口 {@code net.kyori.adventure.audience.ForwardingAudience}。终端方法 audiences() 在 CraftWorldApiMixin 上。</p>
 */
@Mixin(targets = "org.bukkit.World", remap = false)
public interface WorldIfaceMixin extends net.kyori.adventure.audience.ForwardingAudience {

    @Unique
    public abstract int getEntityCount();

    @Unique
    public abstract int getTileEntityCount();

    @Unique
    public abstract int getTickableTileEntityCount();

    @Unique
    public abstract int getChunkCount();

    @Unique
    public abstract int getPlayerCount();

    @Unique
    public abstract org.bukkit.Location findLightningRod(org.bukkit.Location p0);

    @Unique
    public abstract org.bukkit.Location findLightningTarget(org.bukkit.Location p0);

    @Unique
    public abstract java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsync(int p0, int p1, boolean p2, boolean p3);

    // paper-api 把 getChunkAtAsync 的其余重载都写成 default 方法，运行时接口里一个都没有 ——
    // 插件调 getChunkAtAsync(loc) 直接 NoSuchMethodError。方法体照抄 paper-api 的 default 实现。
    // （interface mixin 的 default 方法体会随接口合并进目标，见 A2-1 的实验结论。）

    @Unique
    public default java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsync(int x, int z) {
        return getChunkAtAsync(x, z, true, false);
    }

    @Unique
    public default java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsync(int x, int z, boolean gen) {
        return getChunkAtAsync(x, z, gen, false);
    }

    @Unique
    public default java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsync(org.bukkit.Location loc) {
        return getChunkAtAsync(loc.getBlockX() >> 4, loc.getBlockZ() >> 4, true, false);
    }

    @Unique
    public default java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsync(org.bukkit.Location loc, boolean gen) {
        return getChunkAtAsync(loc.getBlockX() >> 4, loc.getBlockZ() >> 4, gen, false);
    }

    @Unique
    public default java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsync(org.bukkit.block.Block block) {
        return getChunkAtAsync(block.getX() >> 4, block.getZ() >> 4, true, false);
    }

    @Unique
    public default java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsync(org.bukkit.block.Block block, boolean gen) {
        return getChunkAtAsync(block.getX() >> 4, block.getZ() >> 4, gen, false);
    }

    @Unique
    public default java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsyncUrgently(int x, int z) {
        return getChunkAtAsync(x, z, true, true);
    }

    @Unique
    public default java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsyncUrgently(org.bukkit.Location loc) {
        return getChunkAtAsync(loc.getBlockX() >> 4, loc.getBlockZ() >> 4, true, true);
    }

    @Unique
    public default java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsyncUrgently(org.bukkit.Location loc, boolean gen) {
        return getChunkAtAsync(loc.getBlockX() >> 4, loc.getBlockZ() >> 4, gen, true);
    }

    @Unique
    public default java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsyncUrgently(org.bukkit.block.Block block) {
        return getChunkAtAsync(block.getX() >> 4, block.getZ() >> 4, true, true);
    }

    @Unique
    public default java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsyncUrgently(org.bukkit.block.Block block, boolean gen) {
        return getChunkAtAsync(block.getX() >> 4, block.getZ() >> 4, gen, true);
    }

    @Unique
    public abstract org.bukkit.entity.Entity getEntity(java.util.UUID p0);

    @Unique
    public abstract org.bukkit.util.RayTraceResult rayTraceEntities(io.papermc.paper.math.Position p0, org.bukkit.util.Vector p1, double p2, double p3, java.util.function.Predicate p4);

    @Unique
    public abstract boolean isDayTime();

    @Unique
    public abstract boolean createExplosion(org.bukkit.entity.Entity p0, org.bukkit.Location p1, float p2, boolean p3, boolean p4, boolean p5);

    @Unique
    public abstract void spawnParticle(org.bukkit.Particle p0, java.util.List p1, org.bukkit.entity.Player p2, double p3, double p4, double p5, int p6, double p7, double p8, double p9, double p10, java.lang.Object p11, boolean p12);

    @Unique
    public abstract double getCoordinateScale();

    @Unique
    public abstract boolean isFixedTime();

    @Unique
    public abstract java.util.Collection getInfiniburn();

    @Unique
    public abstract void sendGameEvent(org.bukkit.entity.Entity p0, org.bukkit.GameEvent p1, org.bukkit.util.Vector p2);

    @Unique
    public abstract void setViewDistance(int p0);

    @Unique
    public abstract void setSimulationDistance(int p0);

    @Unique
    public abstract int getSendViewDistance();

    @Unique
    public abstract void setSendViewDistance(int p0);
    @Unique
    public abstract org.bukkit.Location locateNearestBiome(org.bukkit.Location location, org.bukkit.block.Biome biome, int radius);

    @Unique
    public abstract org.bukkit.Location locateNearestBiome(org.bukkit.Location location, org.bukkit.block.Biome biome, int radius, int step);
    @Unique
    public abstract boolean isUltrawarm();
    @Unique
    public abstract boolean hasSkylight();
    @Unique
    public abstract boolean hasBedrockCeiling();
    @Unique
    public abstract boolean doesBedWork();
    @Unique
    public abstract boolean doesRespawnAnchorWork();
    @Unique
    public abstract int getNoTickViewDistance();
    @Unique
    public abstract void setNoTickViewDistance(int viewDistance);
}
