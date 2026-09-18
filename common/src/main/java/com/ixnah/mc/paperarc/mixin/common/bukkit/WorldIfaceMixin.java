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

    /** paper-api 1.20.1 的抽象版本（5 个形参），实现体在 CraftWorldApiMixin。 */
    @Unique
    public abstract boolean createExplosion(org.bukkit.entity.Entity p0, org.bukkit.Location p1, float p2, boolean p3, boolean p4);

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
    // ===== A4-4：paper-api 的 default 方法体照搬（运行时接口里一个都没有）=====

    @Unique
    public default boolean createExplosion(org.bukkit.entity.Entity source, float power) {
        return createExplosion(source, power, false, true);
    }

    @Unique
    public default boolean createExplosion(org.bukkit.entity.Entity source, float power, boolean setFire) {
        return createExplosion(source, power, setFire, true);
    }

    @Unique
    public default boolean createExplosion(org.bukkit.entity.Entity source, float power, boolean setFire, boolean breakBlocks) {
        return ((org.bukkit.World) this).createExplosion(source, source.getLocation(), power, setFire, breakBlocks);
    }

    @Unique
    public default boolean createExplosion(org.bukkit.entity.Entity source, org.bukkit.Location loc, float power) {
        return ((org.bukkit.World) this).createExplosion(source, loc, power, true, true);
    }

    @Unique
    public default boolean createExplosion(org.bukkit.entity.Entity source, org.bukkit.Location loc, float power, boolean setFire) {
        return ((org.bukkit.World) this).createExplosion(source, loc, power, setFire, true);
    }

    @Unique
    public default org.bukkit.block.Block getBlockAtKey(long key) {
        int x = (int) ((key << 37) >> 37);
        int y = (int) (key >>> 54);
        int z = (int) ((key << 10) >> 37);
        return ((org.bukkit.World) this).getBlockAt(x, y, z);
    }

    @Unique
    public default org.bukkit.Location getLocationAtKey(long key) {
        int x = (int) ((key << 37) >> 37);
        int y = (int) (key >>> 54);
        int z = (int) ((key << 10) >> 37);
        return new org.bukkit.Location((org.bukkit.World) this, x, y, z);
    }

    @Unique
    public default org.bukkit.Chunk getChunkAt(long chunkKey) {
        return getChunkAt(chunkKey, true);
    }

    @Unique
    public default org.bukkit.Chunk getChunkAt(long chunkKey, boolean generate) {
        return ((org.bukkit.World) this).getChunkAt((int) chunkKey, (int) (chunkKey >> 32), generate);
    }

    @Unique
    public default boolean isChunkGenerated(long chunkKey) {
        return ((org.bukkit.World) this).isChunkGenerated((int) chunkKey, (int) (chunkKey >> 32));
    }

    @Unique
    public default org.bukkit.block.Block getHighestBlockAt(int x, int z, com.destroystokyo.paper.HeightmapType heightmap) {
        return ((org.bukkit.World) this).getBlockAt(x, getHighestBlockYAt(x, z, heightmap), z);
    }

    @Unique
    public default org.bukkit.block.Block getHighestBlockAt(org.bukkit.Location location, com.destroystokyo.paper.HeightmapType heightmap) {
        return getHighestBlockAt(location.getBlockX(), location.getBlockZ(), heightmap);
    }

    @Unique
    public default int getHighestBlockYAt(org.bukkit.Location location, com.destroystokyo.paper.HeightmapType heightmap) {
        return getHighestBlockYAt(location.getBlockX(), location.getBlockZ(), heightmap);
    }

    /**
     * paper-api 里这一条是抽象方法（CraftWorld 实现），运行时没有。
     * 按 Paper 的 CraftWorld 映射转调运行时已有的 {@code getHighestBlockYAt(int, int, HeightMap)}。
     */
    @Unique
    public default int getHighestBlockYAt(int x, int z, com.destroystokyo.paper.HeightmapType heightmap) {
        org.bukkit.HeightMap mapped;
        switch (heightmap) {
            case ANY: mapped = org.bukkit.HeightMap.WORLD_SURFACE; break;
            case SOLID: mapped = org.bukkit.HeightMap.OCEAN_FLOOR; break;
            case SOLID_OR_LIQUID: mapped = org.bukkit.HeightMap.MOTION_BLOCKING; break;
            case SOLID_OR_LIQUID_NO_LEAVES: mapped = org.bukkit.HeightMap.MOTION_BLOCKING_NO_LEAVES; break;
            default:
                // LIGHT_BLOCKING：vanilla 没有对应的 heightmap（Paper 自己也抛这个异常）
                throw new UnsupportedOperationException("LIGHT_BLOCKING heightmap is not supported");
        }
        return ((org.bukkit.World) this).getHighestBlockYAt(x, z, mapped);
    }

    @Unique
    public default java.util.Collection<org.bukkit.entity.Player> getNearbyPlayers(org.bukkit.Location loc, double radius) {
        return getNearbyPlayers(loc, radius, radius, radius, null);
    }

    @Unique
    public default java.util.Collection<org.bukkit.entity.Player> getNearbyPlayers(org.bukkit.Location loc, double xzRadius, double yRadius) {
        return getNearbyPlayers(loc, xzRadius, yRadius, xzRadius, null);
    }

    @Unique
    public default java.util.Collection<org.bukkit.entity.Player> getNearbyPlayers(org.bukkit.Location loc, double xRadius, double yRadius, double zRadius) {
        return getNearbyPlayers(loc, xRadius, yRadius, zRadius, null);
    }

    @Unique
    public default java.util.Collection<org.bukkit.entity.Player> getNearbyPlayers(org.bukkit.Location loc, double radius, java.util.function.Predicate<org.bukkit.entity.Player> predicate) {
        return getNearbyPlayers(loc, radius, radius, radius, predicate);
    }

    @Unique
    public default java.util.Collection<org.bukkit.entity.Player> getNearbyPlayers(org.bukkit.Location loc, double xzRadius, double yRadius, java.util.function.Predicate<org.bukkit.entity.Player> predicate) {
        return getNearbyPlayers(loc, xzRadius, yRadius, xzRadius, predicate);
    }

    @Unique
    public default java.util.Collection<org.bukkit.entity.Player> getNearbyPlayers(org.bukkit.Location loc, double xRadius, double yRadius, double zRadius, java.util.function.Predicate<org.bukkit.entity.Player> predicate) {
        return getNearbyEntitiesByType(org.bukkit.entity.Player.class, loc, xRadius, yRadius, zRadius, predicate);
    }

    @Unique
    public default java.util.Collection<org.bukkit.entity.LivingEntity> getNearbyLivingEntities(org.bukkit.Location loc, double radius) {
        return getNearbyLivingEntities(loc, radius, radius, radius, null);
    }

    @Unique
    public default java.util.Collection<org.bukkit.entity.LivingEntity> getNearbyLivingEntities(org.bukkit.Location loc, double xzRadius, double yRadius) {
        return getNearbyLivingEntities(loc, xzRadius, yRadius, xzRadius, null);
    }

    @Unique
    public default java.util.Collection<org.bukkit.entity.LivingEntity> getNearbyLivingEntities(org.bukkit.Location loc, double xRadius, double yRadius, double zRadius) {
        return getNearbyLivingEntities(loc, xRadius, yRadius, zRadius, null);
    }

    @Unique
    public default java.util.Collection<org.bukkit.entity.LivingEntity> getNearbyLivingEntities(org.bukkit.Location loc, double radius, java.util.function.Predicate<org.bukkit.entity.LivingEntity> predicate) {
        return getNearbyLivingEntities(loc, radius, radius, radius, predicate);
    }

    @Unique
    public default java.util.Collection<org.bukkit.entity.LivingEntity> getNearbyLivingEntities(org.bukkit.Location loc, double xzRadius, double yRadius, java.util.function.Predicate<org.bukkit.entity.LivingEntity> predicate) {
        return getNearbyLivingEntities(loc, xzRadius, yRadius, xzRadius, predicate);
    }

    @Unique
    public default java.util.Collection<org.bukkit.entity.LivingEntity> getNearbyLivingEntities(org.bukkit.Location loc, double xRadius, double yRadius, double zRadius, java.util.function.Predicate<org.bukkit.entity.LivingEntity> predicate) {
        return getNearbyEntitiesByType(org.bukkit.entity.LivingEntity.class, loc, xRadius, yRadius, zRadius, predicate);
    }

    @Unique
    public default <T extends org.bukkit.entity.Entity> java.util.Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, org.bukkit.Location loc, double radius) {
        return getNearbyEntitiesByType(clazz, loc, radius, radius, radius, null);
    }

    @Unique
    public default <T extends org.bukkit.entity.Entity> java.util.Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, org.bukkit.Location loc, double xzRadius, double yRadius) {
        return getNearbyEntitiesByType(clazz, loc, xzRadius, yRadius, xzRadius, null);
    }

    @Unique
    public default <T extends org.bukkit.entity.Entity> java.util.Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, org.bukkit.Location loc, double xRadius, double yRadius, double zRadius) {
        return getNearbyEntitiesByType(clazz, loc, xRadius, yRadius, zRadius, null);
    }

    @Unique
    public default <T extends org.bukkit.entity.Entity> java.util.Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, org.bukkit.Location loc, double radius, java.util.function.Predicate<T> predicate) {
        return getNearbyEntitiesByType(clazz, loc, radius, radius, radius, predicate);
    }

    @Unique
    public default <T extends org.bukkit.entity.Entity> java.util.Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, org.bukkit.Location loc, double xzRadius, double yRadius, java.util.function.Predicate<T> predicate) {
        return getNearbyEntitiesByType(clazz, loc, xzRadius, yRadius, xzRadius, predicate);
    }

    /** paper 的主实现：按 AABB 取实体再按类型/谓词过滤。 */
    @Unique
    @SuppressWarnings("unchecked")
    public default <T extends org.bukkit.entity.Entity> java.util.Collection<T> getNearbyEntitiesByType(Class<? extends org.bukkit.entity.Entity> clazz, org.bukkit.Location loc, double xRadius, double yRadius, double zRadius, java.util.function.Predicate<T> predicate) {
        if (clazz == null) {
            clazz = org.bukkit.entity.Entity.class;
        }
        java.util.List<T> nearby = new java.util.ArrayList<>();
        for (org.bukkit.entity.Entity bukkitEntity : ((org.bukkit.World) this).getNearbyEntities(loc, xRadius, yRadius, zRadius)) {
            if (clazz.isAssignableFrom(bukkitEntity.getClass())
                    && (predicate == null || predicate.test((T) bukkitEntity))) {
                nearby.add((T) bukkitEntity);
            }
        }
        return nearby;
    }

    /** paper 的 12 参重载：默认 force = false。 */
    @Unique
    public default <T> void spawnParticle(org.bukkit.Particle particle, java.util.List<org.bukkit.entity.Player> receivers, org.bukkit.entity.Player source, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
        spawnParticle(particle, receivers, source, x, y, z, count, offsetX, offsetY, offsetZ, extra, data, false);
    }

    /** 实现体在 CraftWorldApiMixin 上（ForwardingAudience 的终端方法）。 */
    @Unique
    public abstract Iterable<? extends net.kyori.adventure.audience.Audience> audiences();

}
