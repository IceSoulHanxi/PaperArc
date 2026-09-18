package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import com.ixnah.mc.paperarc.bridge.api.PaperarcBlockKeys;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * paper 加在 {@code org.bukkit.Location} 上的 37 个方法（checklist §1.10 am，第 ② 批）。
 *
 * <p>方法体逐条照抄 paper-api 反编译结果，只把私有字段访问换成公开 getter/setter。
 * 两处 paper-only 依赖换成运行时等价物：{@code toBlockKey()} 原本调接口 static
 * {@code Block.getBlockKey}（Mixin 只合并实例成员，补不进接口）→ {@code PaperarcBlockKeys}；
 * {@code isFinite()} 原本是 {@code FinePosition.super.isFinite()}（mixin 里写不了
 * {@code Interface.super}）→ 展开成同样的三个 {@code Double.isFinite}。</p>
 *
 * <p>{@code x()/y()/z()} 由 {@link LocationFinePositionMixin} 提供，不在这里重复。</p>
 */
@Mixin(Location.class)
public abstract class LocationApiMixin {

    @Unique
    private Location paperarc$self() {
        return (Location) (Object) this;
    }

    @Unique
    private World paperarc$requireWorld() {
        World world = this.paperarc$self().getWorld();
        if (world == null) {
            throw new IllegalArgumentException("Location has no world");
        }
        return world;
    }

    @Unique
    public boolean isChunkLoaded() {
        Location self = this.paperarc$self();
        return self.getWorld().isChunkLoaded(Location.locToBlock(self.getX()) >> 4,
                Location.locToBlock(self.getZ()) >> 4);
    }

    @Unique
    public boolean isGenerated() {
        Location self = this.paperarc$self();
        World world = self.getWorld();
        Preconditions.checkNotNull(world, "Location has no world!");
        return world.isChunkGenerated(Location.locToBlock(self.getX()) >> 4,
                Location.locToBlock(self.getZ()) >> 4);
    }

    @Unique
    public Location set(double x, double y, double z) {
        Location self = this.paperarc$self();
        self.setX(x);
        self.setY(y);
        self.setZ(z);
        return self;
    }

    @Unique
    public Location add(Location base, double x, double y, double z) {
        return this.set(base.getX() + x, base.getY() + y, base.getZ() + z);
    }

    @Unique
    public Location subtract(Location base, double x, double y, double z) {
        return this.set(base.getX() - x, base.getY() - y, base.getZ() - z);
    }

    @Unique
    public Location toBlockLocation() {
        Location self = this.paperarc$self();
        Location blockLoc = self.clone();
        blockLoc.setX(self.getBlockX());
        blockLoc.setY(self.getBlockY());
        blockLoc.setZ(self.getBlockZ());
        return blockLoc;
    }

    @Unique
    public long toBlockKey() {
        Location self = this.paperarc$self();
        return PaperarcBlockKeys.pack(self.getBlockX(), self.getBlockY(), self.getBlockZ());
    }

    @Unique
    public Location toCenterLocation() {
        Location self = this.paperarc$self();
        Location centerLoc = self.clone();
        centerLoc.setX(self.getBlockX() + 0.5D);
        centerLoc.setY(self.getBlockY() + 0.5D);
        centerLoc.setZ(self.getBlockZ() + 0.5D);
        return centerLoc;
    }

    @Unique
    public Location toHighestLocation() {
        return this.toHighestLocation(HeightMap.WORLD_SURFACE);
    }

    @Unique
    public Location toHighestLocation(HeightMap heightMap) {
        Location self = this.paperarc$self();
        Location ret = self.clone();
        ret.setY(self.getWorld().getHighestBlockYAt(self, heightMap));
        return ret;
    }

    @Unique
    public boolean createExplosion(float power) {
        Location self = this.paperarc$self();
        return self.getWorld().createExplosion(self, power);
    }

    @Unique
    public boolean createExplosion(float power, boolean setFire) {
        Location self = this.paperarc$self();
        return self.getWorld().createExplosion(self, power, setFire);
    }

    @Unique
    public boolean createExplosion(float power, boolean setFire, boolean breakBlocks) {
        Location self = this.paperarc$self();
        return self.getWorld().createExplosion(self, power, setFire, breakBlocks);
    }

    @Unique
    public boolean createExplosion(Entity source, float power) {
        Location self = this.paperarc$self();
        return self.getWorld().createExplosion(source, self, power, true, true);
    }

    @Unique
    public boolean createExplosion(Entity source, float power, boolean setFire) {
        Location self = this.paperarc$self();
        return self.getWorld().createExplosion(source, self, power, setFire, true);
    }

    @Unique
    public boolean createExplosion(Entity source, float power, boolean setFire, boolean breakBlocks) {
        Location self = this.paperarc$self();
        return self.getWorld().createExplosion(source, self, power, setFire, breakBlocks);
    }

    @Unique
    public Collection<Entity> getNearbyEntities(double x, double y, double z) {
        return this.paperarc$requireWorld().getNearbyEntities(this.paperarc$self(), x, y, z);
    }

    @Unique
    public Collection<LivingEntity> getNearbyLivingEntities(double radius) {
        return this.getNearbyEntitiesByType(LivingEntity.class, radius, radius, radius);
    }

    @Unique
    public Collection<LivingEntity> getNearbyLivingEntities(double xzRadius, double yRadius) {
        return this.getNearbyEntitiesByType(LivingEntity.class, xzRadius, yRadius, xzRadius);
    }

    @Unique
    public Collection<LivingEntity> getNearbyLivingEntities(double xRadius, double yRadius, double zRadius) {
        return this.getNearbyEntitiesByType(LivingEntity.class, xRadius, yRadius, zRadius);
    }

    @Unique
    public Collection<LivingEntity> getNearbyLivingEntities(double radius, Predicate<? super LivingEntity> predicate) {
        return this.getNearbyEntitiesByType(LivingEntity.class, radius, radius, radius, predicate);
    }

    @Unique
    public Collection<LivingEntity> getNearbyLivingEntities(double xzRadius, double yRadius,
                                                            Predicate<? super LivingEntity> predicate) {
        return this.getNearbyEntitiesByType(LivingEntity.class, xzRadius, yRadius, xzRadius, predicate);
    }

    @Unique
    public Collection<LivingEntity> getNearbyLivingEntities(double xRadius, double yRadius, double zRadius,
                                                            Predicate<? super LivingEntity> predicate) {
        return this.getNearbyEntitiesByType(LivingEntity.class, xRadius, yRadius, zRadius, predicate);
    }

    @Unique
    public Collection<Player> getNearbyPlayers(double radius) {
        return this.getNearbyEntitiesByType(Player.class, radius, radius, radius);
    }

    @Unique
    public Collection<Player> getNearbyPlayers(double xzRadius, double yRadius) {
        return this.getNearbyEntitiesByType(Player.class, xzRadius, yRadius, xzRadius);
    }

    @Unique
    public Collection<Player> getNearbyPlayers(double xRadius, double yRadius, double zRadius) {
        return this.getNearbyEntitiesByType(Player.class, xRadius, yRadius, zRadius);
    }

    @Unique
    public Collection<Player> getNearbyPlayers(double radius, Predicate<? super Player> predicate) {
        return this.getNearbyEntitiesByType(Player.class, radius, radius, radius, predicate);
    }

    @Unique
    public Collection<Player> getNearbyPlayers(double xzRadius, double yRadius, Predicate<? super Player> predicate) {
        return this.getNearbyEntitiesByType(Player.class, xzRadius, yRadius, xzRadius, predicate);
    }

    @Unique
    public Collection<Player> getNearbyPlayers(double xRadius, double yRadius, double zRadius,
                                               Predicate<? super Player> predicate) {
        return this.getNearbyEntitiesByType(Player.class, xRadius, yRadius, zRadius, predicate);
    }

    @Unique
    public <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, double radius) {
        return this.getNearbyEntitiesByType(clazz, radius, radius, radius, (Predicate<? super T>) null);
    }

    @Unique
    public <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, double xzRadius,
                                                                    double yRadius) {
        return this.getNearbyEntitiesByType(clazz, xzRadius, yRadius, xzRadius, (Predicate<? super T>) null);
    }

    @Unique
    public <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, double xRadius,
                                                                    double yRadius, double zRadius) {
        return this.getNearbyEntitiesByType(clazz, xRadius, yRadius, zRadius, (Predicate<? super T>) null);
    }

    @Unique
    public <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, double radius,
                                                                    Predicate<? super T> predicate) {
        return this.getNearbyEntitiesByType(clazz, radius, radius, radius, predicate);
    }

    @Unique
    public <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, double xzRadius,
                                                                    double yRadius, Predicate<? super T> predicate) {
        return this.getNearbyEntitiesByType(clazz, xzRadius, yRadius, xzRadius, predicate);
    }

    @Unique
    public <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends Entity> clazz, double xRadius,
                                                                    double yRadius, double zRadius,
                                                                    Predicate<? super T> predicate) {
        return this.paperarc$requireWorld()
                .getNearbyEntitiesByType(clazz, this.paperarc$self(), xRadius, yRadius, zRadius, predicate);
    }

    @Unique
    public boolean isFinite() {
        Location self = this.paperarc$self();
        return Double.isFinite(self.getX()) && Double.isFinite(self.getY()) && Double.isFinite(self.getZ())
                && Float.isFinite(self.getYaw()) && Float.isFinite(self.getPitch());
    }

    @Unique
    public Location toLocation(World world) {
        Location self = this.paperarc$self();
        return new Location(world, self.getX(), self.getY(), self.getZ(), self.getYaw(), self.getPitch());
    }
}
