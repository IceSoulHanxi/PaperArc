package com.ixnah.mc.paperarc.mixin.common.bukkit;

import com.ixnah.mc.paperarc.bridge.api.PaperarcBlockKeys;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import io.papermc.paper.math.Position;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.generator.structure.GeneratedStructure;
import org.bukkit.generator.structure.Structure;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.Metadatable;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageRecipient;
import org.bukkit.util.BiomeSearchResult;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.StructureSearchResult;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.World} (generated).
 * Adds 31 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.World", remap = false)
public interface WorldIfaceMixin extends net.kyori.adventure.audience.ForwardingAudience {

    @Unique
    public abstract boolean isVoidDamageEnabled();

    @Unique
    public abstract void setVoidDamageEnabled(boolean p0);

    @Unique
    public abstract float getVoidDamageAmount();

    @Unique
    public abstract void setVoidDamageAmount(float p0);

    @Unique
    public abstract double getVoidDamageMinBuildHeightOffset();

    @Unique
    public abstract void setVoidDamageMinBuildHeightOffset(double p0);

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
    public abstract boolean hasStructureAt(io.papermc.paper.math.Position p0, org.bukkit.generator.structure.Structure p1);

    @Unique
    public abstract org.bukkit.Location findLightningRod(org.bukkit.Location p0);

    @Unique
    public abstract org.bukkit.Location findLightningTarget(org.bukkit.Location p0);

    @Unique
    public abstract java.util.concurrent.CompletableFuture<org.bukkit.Chunk> getChunkAtAsync(int p0, int p1, boolean p2, boolean p3);

    // paper-api 把 getChunkAtAsync 的其余重载都写成 default 方法，运行时接口里一个都没有 ——
    // 插件调 getChunkAtAsync(loc) 直接 NoSuchMethodError。方法体照抄 paper-api 的 default 实现。
    //（interface mixin 的 default 方法体会随接口合并进目标，见 B4b。）

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
    public abstract org.bukkit.util.RayTraceResult rayTraceBlocks(io.papermc.paper.math.Position p0, org.bukkit.util.Vector p1, double p2, org.bukkit.FluidCollisionMode p3, boolean p4, java.util.function.Predicate p5);

    @Unique
    public abstract org.bukkit.util.RayTraceResult rayTrace(io.papermc.paper.math.Position p0, org.bukkit.util.Vector p1, double p2, org.bukkit.FluidCollisionMode p3, boolean p4, double p5, java.util.function.Predicate p6, java.util.function.Predicate p7);

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
    public abstract org.bukkit.Raid getRaid(int p0);

    @Unique
    public abstract void setViewDistance(int p0);

    @Unique
    public abstract void setSimulationDistance(int p0);

    @Unique
    public abstract int getSendViewDistance();

    @Unique
    public abstract void setSendViewDistance(int p0);

    /** paper {@code World#audiences()} 的 default 方法体：世界里的玩家。 */
    @Unique
    public default Iterable<? extends net.kyori.adventure.audience.Audience> audiences() {
        return ((org.bukkit.World) this).getPlayers();
    }

    @Unique
    public default boolean isPositionLoaded(Position position) {
        World self = (World) this;
        return self.isChunkLoaded(position.blockX() >> 4, position.blockZ() >> 4);
    }

    @Unique
    public default Block getBlockAtKey(long key) {
        World self = (World) this;
        int x = PaperarcBlockKeys.unpackX(key);
        int y = PaperarcBlockKeys.unpackY(key);
        int z = PaperarcBlockKeys.unpackZ(key);

        return self.getBlockAt(x, y, z);
    }

    @Unique
    public default Location getLocationAtKey(long key) {
        World self = (World) this;
        int x = PaperarcBlockKeys.unpackX(key);
        int y = PaperarcBlockKeys.unpackY(key);
        int z = PaperarcBlockKeys.unpackZ(key);

        return new Location(self, (double) x, (double) y, (double) z);
    }

    @Unique
    public default Chunk getChunkAt(long chunkKey) {
        World self = (World) this;
        return self.getChunkAt(chunkKey, true);
    }

    @Unique
    public default Chunk getChunkAt(long chunkKey, boolean generate) {
        World self = (World) this;
        return self.getChunkAt((int) chunkKey, (int) (chunkKey >> 32), generate);
    }

    @Unique
    public default boolean isChunkGenerated(long chunkKey) {
        World self = (World) this;
        return self.isChunkGenerated((int) chunkKey, (int) (chunkKey >> 32));
    }

    @Unique
    public default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double radius) {
        World self = (World) this;
        return self.getNearbyEntitiesByType(LivingEntity.class, loc, radius, radius, radius);
    }

    @Unique
    public default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double xzRadius, double yRadius) {
        World self = (World) this;
        return self.getNearbyEntitiesByType(LivingEntity.class, loc, xzRadius, yRadius, xzRadius);
    }

    @Unique
    public default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double xRadius, double yRadius, double zRadius) {
        World self = (World) this;
        return self.getNearbyEntitiesByType(LivingEntity.class, loc, xRadius, yRadius, zRadius);
    }

    @Unique
    public default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double radius, Predicate<? super LivingEntity> predicate) {
        World self = (World) this;
        return self.getNearbyEntitiesByType(LivingEntity.class, loc, radius, radius, radius, predicate);
    }

    @Unique
    public default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double xzRadius, double yRadius, Predicate<? super LivingEntity> predicate) {
        World self = (World) this;
        return self.getNearbyEntitiesByType(LivingEntity.class, loc, xzRadius, yRadius, xzRadius, predicate);
    }

    @Unique
    public default Collection<LivingEntity> getNearbyLivingEntities(Location loc, double xRadius, double yRadius, double zRadius, Predicate<? super LivingEntity> predicate) {
        World self = (World) this;
        return self.getNearbyEntitiesByType(LivingEntity.class, loc, xRadius, yRadius, zRadius, predicate);
    }

    @Unique
    public default Collection<Player> getNearbyPlayers(Location loc, double radius) {
        World self = (World) this;
        return self.getNearbyEntitiesByType(Player.class, loc, radius, radius, radius);
    }

    @Unique
    public default Collection<Player> getNearbyPlayers(Location loc, double xzRadius, double yRadius) {
        World self = (World) this;
        return self.getNearbyEntitiesByType(Player.class, loc, xzRadius, yRadius, xzRadius);
    }

    @Unique
    public default Collection<Player> getNearbyPlayers(Location loc, double xRadius, double yRadius, double zRadius) {
        World self = (World) this;
        return self.getNearbyEntitiesByType(Player.class, loc, xRadius, yRadius, zRadius);
    }

    @Unique
    public default Collection<Player> getNearbyPlayers(Location loc, double radius, Predicate<? super Player> predicate) {
        World self = (World) this;
        return self.getNearbyEntitiesByType(Player.class, loc, radius, radius, radius, predicate);
    }

    @Unique
    public default Collection<Player> getNearbyPlayers(Location loc, double xzRadius, double yRadius, Predicate<? super Player> predicate) {
        World self = (World) this;
        return self.getNearbyEntitiesByType(Player.class, loc, xzRadius, yRadius, xzRadius, predicate);
    }

    @Unique
    public default Collection<Player> getNearbyPlayers(Location loc, double xRadius, double yRadius, double zRadius, Predicate<? super Player> predicate) {
        World self = (World) this;
        return self.getNearbyEntitiesByType(Player.class, loc, xRadius, yRadius, zRadius, predicate);
    }

    @Unique
    public default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, Location loc, double radius) {
        World self = (World) this;
        return self.getNearbyEntitiesByType(clazz, loc, radius, radius, radius, (Predicate) null);
    }

    @Unique
    public default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, Location loc, double xzRadius, double yRadius) {
        World self = (World) this;
        return self.getNearbyEntitiesByType(clazz, loc, xzRadius, yRadius, xzRadius, (Predicate) null);
    }

    @Unique
    public default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, Location loc, double xRadius, double yRadius, double zRadius) {
        World self = (World) this;
        return self.getNearbyEntitiesByType(clazz, loc, xRadius, yRadius, zRadius, (Predicate) null);
    }

    @Unique
    public default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, Location loc, double radius, Predicate<? super T> predicate) {
        World self = (World) this;
        return self.getNearbyEntitiesByType(clazz, loc, radius, radius, radius, predicate);
    }

    @Unique
    public default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends T> clazz, Location loc, double xzRadius, double yRadius, Predicate<? super T> predicate) {
        World self = (World) this;
        return self.getNearbyEntitiesByType(clazz, loc, xzRadius, yRadius, xzRadius, predicate);
    }

    @Unique
    public default <T extends Entity> Collection<T> getNearbyEntitiesByType(Class<? extends Entity> clazz, Location loc, double xRadius, double yRadius, double zRadius, Predicate<? super T> predicate) {
        World self = (World) this;
        if (clazz == null) {
            clazz = Entity.class;
        }

        List<T> nearby = new ArrayList<>();
        Iterator iterator = self.getNearbyEntities(loc, xRadius, yRadius, zRadius).iterator();

        while (iterator.hasNext()) {
            Entity bukkitEntity = (Entity) iterator.next();

            @SuppressWarnings("unchecked")
            T typed = (T) bukkitEntity;
            if (clazz.isAssignableFrom(bukkitEntity.getClass())
                    && (predicate == null || predicate.test(typed))) {
                nearby.add(typed);
            }
        }

        return nearby;
    }

    @Unique
    public default boolean createExplosion(Entity source, Location loc, float power, boolean setFire, boolean breakBlocks) {
        World self = (World) this;
        return self.createExplosion(source, loc, power, setFire, breakBlocks, true);
    }

    @Unique
    public default boolean createExplosion(Entity source, Location loc, float power, boolean setFire) {
        World self = (World) this;
        return self.createExplosion(source, loc, power, setFire, true);
    }

    @Unique
    public default boolean createExplosion(Entity source, Location loc, float power) {
        World self = (World) this;
        return self.createExplosion(source, loc, power, true, true);
    }

    @Unique
    public default boolean createExplosion(Entity source, float power, boolean setFire, boolean breakBlocks) {
        World self = (World) this;
        return self.createExplosion(source, source.getLocation(), power, setFire, breakBlocks);
    }

    @Unique
    public default boolean createExplosion(Entity source, float power, boolean setFire) {
        World self = (World) this;
        return self.createExplosion(source, source.getLocation(), power, setFire, true);
    }

    @Unique
    public default boolean createExplosion(Entity source, float power) {
        World self = (World) this;
        return self.createExplosion(source, source.getLocation(), power, true, true);
    }

    @Unique
    public default <T> void spawnParticle(Particle particle, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
        World self = (World) this;
        self.spawnParticle(particle, (List) null, (Player) null, x, y, z, count, offsetX, offsetY, offsetZ, extra, data, true);
    }

    @Unique
    public default <T> void spawnParticle(Particle particle, List<Player> receivers, Player source, double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ, double extra, T data) {
        World self = (World) this;
        self.spawnParticle(particle, receivers, source, x, y, z, count, offsetX, offsetY, offsetZ, extra, data, true);
    }

    @Unique
    public default Location locateNearestBiome(Location origin, Biome biome, int radius) {
        World self = (World) this;
        return Optional.ofNullable(self.locateNearestBiome(origin, radius, 8, 8, biome))
                .map(BiomeSearchResult::getLocation).orElse(null);
    }

    @Unique
    public default Location locateNearestBiome(Location origin, Biome biome, int radius, int step) {
        World self = (World) this;
        return Optional.ofNullable(self.locateNearestBiome(origin, radius, step, step, biome))
                .map(BiomeSearchResult::getLocation).orElse(null);
    }

    @Unique
    public default int getNoTickViewDistance() {
        World self = (World) this;
        return self.getViewDistance();
    }

    @Unique
    public default void setNoTickViewDistance(int viewDistance) {
        World self = (World) this;
        self.setViewDistance(viewDistance);
    }
}
