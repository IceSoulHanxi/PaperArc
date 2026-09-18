package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.RegionAccessor} (generated, trimmed for 1.20.1).
 * Adds 5 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 *
 * <p>A4-3 父接口差集：补上 paper 声明的父接口 {@code org.bukkit.Keyed}。终端方法 getKey() 在 CraftWorld 上运行时已有，CraftLimitedRegion 由 CraftLimitedRegionApiMixin 补。</p>
 */
@Mixin(targets = "org.bukkit.RegionAccessor", remap = false)
public interface RegionAccessorIfaceMixin extends org.bukkit.Keyed {

    @Unique
    public abstract org.bukkit.block.Biome getComputedBiome(int p0, int p1, int p2);

    @Unique
    public abstract io.papermc.paper.world.MoonPhase getMoonPhase();

    @Unique
    public abstract org.bukkit.NamespacedKey getKey();

    @Unique
    public abstract boolean lineOfSightExists(org.bukkit.Location p0, org.bukkit.Location p1);

    @Unique
    public abstract boolean hasCollisionsIn(org.bukkit.util.BoundingBox p0);
    /**
     * 运行时 {@code CraftRegionAccessor} 上已有这个实现（javap 核对），但
     * {@code RegionAccessor} 接口上没有 —— 下面那个 default 方法体正是经接口调它的，
     * 不声明就是 {@code NoSuchMethodError}（A6/X-2 的 check-api-descriptors 抓出）。
     */
    @Unique
    public abstract <T extends org.bukkit.entity.Entity> T spawn(org.bukkit.Location p0, Class<T> p1,
            org.bukkit.util.Consumer<T> p2,
            org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason p3) throws IllegalArgumentException;

    // ===== A4-4：paper-api 的 default 方法体照搬（运行时接口里一个都没有）=====

    @Unique
    public default <T extends org.bukkit.entity.Entity> T spawn(org.bukkit.Location location, Class<T> clazz,
            org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) throws IllegalArgumentException {
        return spawn(location, clazz, reason, null);
    }

    @Unique
    public default <T extends org.bukkit.entity.Entity> T spawn(org.bukkit.Location location, Class<T> clazz,
            org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason,
            org.bukkit.util.Consumer<T> function) throws IllegalArgumentException {
        return ((org.bukkit.RegionAccessor) this).spawn(location, clazz, function, reason);
    }

    @Unique
    public default org.bukkit.entity.Entity spawnEntity(org.bukkit.Location loc, org.bukkit.entity.EntityType type,
            org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason) {
        return spawnEntity(loc, type, reason, null);
    }

    @Unique
    @SuppressWarnings("unchecked")
    public default org.bukkit.entity.Entity spawnEntity(org.bukkit.Location loc, org.bukkit.entity.EntityType type,
            org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason reason,
            org.bukkit.util.Consumer<org.bukkit.entity.Entity> function) {
        return ((org.bukkit.RegionAccessor) this).spawn(loc,
                (Class<org.bukkit.entity.Entity>) type.getEntityClass(), function, reason);
    }

}
