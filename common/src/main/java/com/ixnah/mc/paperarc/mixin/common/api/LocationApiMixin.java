package com.ixnah.mc.paperarc.mixin.common.api;

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
 * paper 加在 {@code org.bukkit.Location} 上的方法（checklist §1.10 am 第二批，共 37 条）。
 *
 * <p>全部按 paper 的 {@code Location.java} 原样实现：坐标操作自己算，其余一律委托
 * {@code World}（{@code createExplosion}/{@code getNearby*}/{@code isChunkLoaded}/
 * {@code isChunkGenerated}/{@code getHighestBlockYAt}）—— World 侧的 paper 方法在
 * A2/A4 已补齐，转发不会落到运行时不存在的成员（{@code checkRuntimeApiCalls} 门禁把关）。
 *
 * <p>{@code toBlockKey()} 里的位运算照抄 paper 的 {@code Block#getBlockKey(int,int,int)}
 * 而不是调它：那是 {@code Block} 接口上的 paper **静态**方法，运行时接口没有静态方法就
 * 没法补，直接内联最省事、语义一致。
 *
 * <p>{@code implements FinePosition} 那条在 {@link LocationFinePositionMixin}（A5-2 已做）。
 */
@Mixin(Location.class)
public abstract class LocationApiMixin {

    @Unique
    private Location paperarc$self() {
        return (Location) (Object) this;
    }

    @Unique
    private World paperarc$world() {
        World world = this.paperarc$self().getWorld();
        if (world == null) {
            throw new IllegalArgumentException("Location has no world");
        }
        return world;
    }

    // ---- 坐标操作（Expand-Location-Manipulation-API / toBlockLocation-toCenterLocation）----

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
    public Location toCenterLocation() {
        Location self = this.paperarc$self();
        Location centerLoc = self.clone();
        centerLoc.setX(self.getBlockX() + 0.5);
        centerLoc.setY(self.getBlockY() + 0.5);
        centerLoc.setZ(self.getBlockZ() + 0.5);
        return centerLoc;
    }

    @Unique
    public long toBlockKey() {
        Location self = this.paperarc$self();
        return ((long) self.getBlockX() & 0x7FFFFFF)
                | (((long) self.getBlockZ() & 0x7FFFFFF) << 27)
                | ((long) self.getBlockY() << 54);
    }

    @Unique
    public Location toLocation(World world) {
        Location self = this.paperarc$self();
        return new Location(world, self.getX(), self.getY(), self.getZ(),
                self.getYaw(), self.getPitch());
    }

    // ---- 区块状态 ----

    @Unique
    public boolean isChunkLoaded() {
        Location self = this.paperarc$self();
        return this.paperarc$world()
                .isChunkLoaded(Location.locToBlock(self.getX()) >> 4, Location.locToBlock(self.getZ()) >> 4);
    }

    @Unique
    public boolean isGenerated() {
        Location self = this.paperarc$self();
        return this.paperarc$world()
                .isChunkGenerated(Location.locToBlock(self.getX()) >> 4, Location.locToBlock(self.getZ()) >> 4);
    }

    // ---- 高度图（Add-Heightmap-API）----

    @Unique
    public Location toHighestLocation() {
        return this.paperarc$self().toHighestLocation(HeightMap.WORLD_SURFACE);
    }

    @Unique
    public Location toHighestLocation(com.destroystokyo.paper.HeightmapType heightmap) {
        Location self = this.paperarc$self();
        Location ret = self.clone();
        ret.setY(this.paperarc$world().getHighestBlockYAt(self, heightmap));
        return ret;
    }

    @Unique
    public Location toHighestLocation(HeightMap heightMap) {
        Location self = this.paperarc$self();
        Location ret = self.clone();
        ret.setY(this.paperarc$world().getHighestBlockYAt(self, heightMap));
        return ret;
    }

    // ---- 爆炸（Expand-Explosions-API）----

    @Unique
    public boolean createExplosion(float power) {
        return this.paperarc$world().createExplosion(this.paperarc$self(), power);
    }

    @Unique
    public boolean createExplosion(float power, boolean setFire) {
        return this.paperarc$world().createExplosion(this.paperarc$self(), power, setFire);
    }

    @Unique
    public boolean createExplosion(float power, boolean setFire, boolean breakBlocks) {
        return this.paperarc$world().createExplosion(this.paperarc$self(), power, setFire, breakBlocks);
    }

    @Unique
    public boolean createExplosion(Entity source, float power) {
        return this.paperarc$world().createExplosion(source, this.paperarc$self(), power, true, true);
    }

    @Unique
    public boolean createExplosion(Entity source, float power, boolean setFire) {
        return this.paperarc$world().createExplosion(source, this.paperarc$self(), power, setFire, true);
    }

    @Unique
    public boolean createExplosion(Entity source, float power, boolean setFire, boolean breakBlocks) {
        return this.paperarc$world()
                .createExplosion(source, this.paperarc$self(), power, setFire, breakBlocks);
    }

    // ---- getNearbyXXX（Add-getNearbyXXX-methods-to-Location）----

    @Unique
    public Collection<Entity> getNearbyEntities(double x, double y, double z) {
        return this.paperarc$world().getNearbyEntities(this.paperarc$self(), x, y, z);
    }

    @Unique
    public <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends Entity> clazz,
                                                                    double xRadius, double yRadius,
                                                                    double zRadius,
                                                                    Predicate<T> predicate) {
        return this.paperarc$world().getNearbyEntitiesByType(
                clazz, this.paperarc$self(), xRadius, yRadius, zRadius, predicate);
    }

    @Unique
    public <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, double radius) {
        return this.getNearbyEntitiesByType(clazz, radius, radius, radius, null);
    }

    @Unique
    public <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz,
                                                                    double xzRadius, double yRadius) {
        return this.getNearbyEntitiesByType(clazz, xzRadius, yRadius, xzRadius, null);
    }

    @Unique
    public <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz,
                                                                    double xRadius, double yRadius,
                                                                    double zRadius) {
        return this.getNearbyEntitiesByType(clazz, xRadius, yRadius, zRadius, null);
    }

    @Unique
    public <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, double radius,
                                                                    Predicate<T> predicate) {
        return this.getNearbyEntitiesByType(clazz, radius, radius, radius, predicate);
    }

    @Unique
    public <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz,
                                                                    double xzRadius, double yRadius,
                                                                    Predicate<T> predicate) {
        return this.getNearbyEntitiesByType(clazz, xzRadius, yRadius, xzRadius, predicate);
    }

    @Unique
    public Collection<LivingEntity> getNearbyLivingEntities(double radius) {
        return this.getNearbyEntitiesByType(LivingEntity.class, radius, radius, radius, null);
    }

    @Unique
    public Collection<LivingEntity> getNearbyLivingEntities(double xzRadius, double yRadius) {
        return this.getNearbyEntitiesByType(LivingEntity.class, xzRadius, yRadius, xzRadius, null);
    }

    @Unique
    public Collection<LivingEntity> getNearbyLivingEntities(double xRadius, double yRadius, double zRadius) {
        return this.getNearbyEntitiesByType(LivingEntity.class, xRadius, yRadius, zRadius, null);
    }

    @Unique
    public Collection<LivingEntity> getNearbyLivingEntities(double radius,
                                                            Predicate<LivingEntity> predicate) {
        return this.getNearbyEntitiesByType(LivingEntity.class, radius, radius, radius, predicate);
    }

    @Unique
    public Collection<LivingEntity> getNearbyLivingEntities(double xzRadius, double yRadius,
                                                            Predicate<LivingEntity> predicate) {
        return this.getNearbyEntitiesByType(LivingEntity.class, xzRadius, yRadius, xzRadius, predicate);
    }

    @Unique
    public Collection<LivingEntity> getNearbyLivingEntities(double xRadius, double yRadius, double zRadius,
                                                            Predicate<LivingEntity> predicate) {
        return this.getNearbyEntitiesByType(LivingEntity.class, xRadius, yRadius, zRadius, predicate);
    }

    @Unique
    public Collection<Player> getNearbyPlayers(double radius) {
        return this.getNearbyEntitiesByType(Player.class, radius, radius, radius, null);
    }

    @Unique
    public Collection<Player> getNearbyPlayers(double xzRadius, double yRadius) {
        return this.getNearbyEntitiesByType(Player.class, xzRadius, yRadius, xzRadius, null);
    }

    @Unique
    public Collection<Player> getNearbyPlayers(double xRadius, double yRadius, double zRadius) {
        return this.getNearbyEntitiesByType(Player.class, xRadius, yRadius, zRadius, null);
    }

    @Unique
    public Collection<Player> getNearbyPlayers(double radius, Predicate<Player> predicate) {
        return this.getNearbyEntitiesByType(Player.class, radius, radius, radius, predicate);
    }

    @Unique
    public Collection<Player> getNearbyPlayers(double xzRadius, double yRadius,
                                               Predicate<Player> predicate) {
        return this.getNearbyEntitiesByType(Player.class, xzRadius, yRadius, xzRadius, predicate);
    }

    @Unique
    public Collection<Player> getNearbyPlayers(double xRadius, double yRadius, double zRadius,
                                               Predicate<Player> predicate) {
        return this.getNearbyEntitiesByType(Player.class, xRadius, yRadius, zRadius, predicate);
    }
}
