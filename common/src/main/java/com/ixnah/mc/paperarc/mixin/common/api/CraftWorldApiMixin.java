package com.ixnah.mc.paperarc.mixin.common.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import com.google.common.base.Preconditions;

import com.mojang.datafixers.util.Pair;
import io.papermc.paper.math.Position;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R1.CraftGameEvent;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftLocation;
import org.bukkit.craftbukkit.v1_20_R1.CraftParticle;
import org.bukkit.craftbukkit.v1_20_R1.CraftRaid;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.generator.structure.CraftStructure;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.ixnah.mc.paperarc.bridge.ApiState;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
/**
 * Adds Paper's CraftWorld APIs missing from Arclight's Spigot CraftBukkit.
 * Batch B24 (docs/api-slices/B24.json).
 */
@Mixin(CraftWorld.class)
public abstract class CraftWorldApiMixin {

    @Shadow
    public abstract ServerLevel getHandle();

    @Shadow
    public abstract int getViewDistance();

    @Shadow
    public abstract org.bukkit.Chunk getChunkAt(int x, int z, boolean generate);

    @Shadow
    public abstract RayTraceResult rayTraceBlocks(Location start, Vector direction, double maxDistance,
            FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks);

    @Shadow
    public abstract RayTraceResult rayTraceEntities(Location start, Vector direction, double maxDistance,
            double raySize, Predicate<? super org.bukkit.entity.Entity> filter);

    @Shadow
    public abstract <T> void spawnParticle(org.bukkit.Particle particle, double x, double y, double z, int count,
            double offsetX, double offsetY, double offsetZ, double extra, T data, boolean forceShow);

    @Unique
    public double getCoordinateScale() {
        return this.getHandle().dimensionType().coordinateScale();
    }

    @Unique
    public boolean isUltrawarm() {
        return this.getHandle().dimensionType().ultraWarm();
    }

    @Unique
    public boolean hasSkylight() {
        return this.getHandle().dimensionType().hasSkyLight();
    }

    @Unique
    public boolean hasBedrockCeiling() {
        // Paper 原样：hasBedrockCeiling 误映射为 hasSkyLight（上游笔误保留）
        return this.getHandle().dimensionType().hasSkyLight();
    }

    @Unique
    public boolean doesBedWork() {
        return this.getHandle().dimensionType().bedWorks();
    }

    @Unique
    public boolean doesRespawnAnchorWork() {
        return this.getHandle().dimensionType().respawnAnchorWorks();
    }

    @Unique
    public boolean isFixedTime() {
        return this.getHandle().dimensionType().fixedTime().isPresent();
    }

    @Unique
    public boolean isDayTime() {
        return this.getHandle().isDay();
    }

    @Unique
    public int getPlayerCount() {
        return this.getHandle().players().size();
    }

    @Unique
    public int getChunkCount() {
        return this.getHandle().getChunkSource().getLoadedChunksCount();
    }

    @Unique
    public int getEntityCount() {
        int count = 0;
        for (Object ignored : this.getHandle().getAllEntities()) {
            count++;
        }
        return count;
    }

    @Unique
    public org.bukkit.entity.Entity getEntity(UUID uuid) {
        Preconditions.checkArgument(uuid != null, "uuid cannot be null");
        Entity nms = this.getHandle().getEntity(uuid);
        // getBukkitEntity() 为 CraftBukkit 运行时注入方法，编译期不可见，走桥接工厂
        return nms == null || nms.isRemoved() ? null : PaperArcBridge.bukkitEntity(nms);
    }

    @Unique
    public Collection<Material> getInfiniburn() {
        Registry<net.minecraft.world.level.block.Block> blocks =
                this.getHandle().registryAccess().registryOrThrow(Registries.BLOCK);
        Collection<Material> materials = new ArrayList<>();
        blocks.getTag(this.getHandle().dimensionType().infiniburn())
                .ifPresent(named -> {
                    for (Holder<net.minecraft.world.level.block.Block> holder : named) {
                        materials.add(CraftMagicNumbers.getMaterial(holder.value()));
                    }
                });
        return Collections.unmodifiableCollection(materials);
    }

    @Unique
    public org.bukkit.Raid getRaid(int id) {
        net.minecraft.world.entity.raid.Raid nms = this.getHandle().getRaids().get(id);
        return nms == null ? null : new CraftRaid(nms);
    }

    @Unique
    public CompletableFuture<org.bukkit.Chunk> getChunkAtAsync(int x, int z, boolean gen, boolean urgent) {
        // sync-fallback: Arclight 无 Paper 异步 chunk 调度器，主线程同步取块
        return CompletableFuture.completedFuture(this.getChunkAt(x, z, gen));
    }

    @Unique
    public int getSendViewDistance() {
        return ApiState.get(this, "sendViewDistance", this.getViewDistance() + 1);
    }

    @Unique
    public void setSendViewDistance(int sendViewDistance) {
        Preconditions.checkArgument(sendViewDistance >= -1, "sendViewDistance must be >= -1");
        ApiState.put(this, "sendViewDistance", sendViewDistance);
    }

    @Unique
    public void setViewDistance(int viewDistance) {
        // vanilla 视距是服务器全局的；这里仅记录每世界覆写值
        Preconditions.checkArgument(viewDistance >= -1, "viewDistance must be >= -1");
        ApiState.put(this, "viewDistance", viewDistance);
    }

    @Unique
    public void setSimulationDistance(int simulationDistance) {
        Preconditions.checkArgument(simulationDistance >= -1, "simulationDistance must be >= -1");
        ApiState.put(this, "simulationDistance", simulationDistance);
    }

    @Unique
    public int getNoTickViewDistance() {
        // vanilla 无 no-tick 视距概念，Paper 补丁本身也委托 getViewDistance()
        return this.getViewDistance();
    }

    @Unique
    public void setNoTickViewDistance(int viewDistance) {
        this.setViewDistance(viewDistance);
    }

    @Unique
    public org.bukkit.Location locateNearestBiome(Location origin, Biome biome, int radius) {
        return this.locateNearestBiome(origin, biome, radius, 8);
    }

    @Unique
    public org.bukkit.Location locateNearestBiome(Location origin, Biome biome, int radius, int step) {
        BlockPos originPos = CraftLocation.toBlockPosition(origin);
        Pair<BlockPos, net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>> pair =
                this.getHandle().findClosestBiome3d(
                        holder -> holder.is(CraftNamespacedKey.toMinecraft(biome.getKey())),
                        originPos, radius, step, step);
        if (pair == null) {
            return null;
        }
        BlockPos nearest = pair.getFirst();
        return new Location((World) (Object) this, nearest.getX(), nearest.getY(), nearest.getZ());
    }

    @Unique
    public boolean isVoidDamageEnabled() {
        return ApiState.get(this, "voidDamageEnabled", Boolean.TRUE);
    }

    @Unique
    public void setVoidDamageEnabled(boolean enabled) {
        ApiState.put(this, "voidDamageEnabled", enabled);
    }

    @Unique
    public float getVoidDamageAmount() {
        return ApiState.get(this, "voidDamageAmount", 4.0F);
    }

    @Unique
    public void setVoidDamageAmount(float amount) {
        Preconditions.checkArgument(amount >= 0.0F, "amount must be >= 0");
        ApiState.put(this, "voidDamageAmount", amount);
    }

    @Unique
    public double getVoidDamageMinBuildHeightOffset() {
        return ApiState.get(this, "voidDamageMinBuildHeightOffset", 0.0D);
    }

    @Unique
    public void setVoidDamageMinBuildHeightOffset(double offset) {
        ApiState.put(this, "voidDamageMinBuildHeightOffset", offset);
    }

    @Unique
    public int getTileEntityCount() {
        // ChunkMap#getChunks 由 AT 加宽（m_140416_）后直访
        int count = 0;
        for (ChunkHolder holder : this.getHandle().getChunkSource().chunkMap.getChunks()) {
            net.minecraft.world.level.chunk.LevelChunk chunk = holder.getTickingChunk();
            if (chunk != null) {
                count += chunk.getBlockEntitiesPos().size();
            }
        }
        return count;
    }

    @Unique
    public int getTickableTileEntityCount() {
        // Level.blockEntityTickers 由 AT 加宽（f_151512_）后直访
        return this.getHandle().blockEntityTickers.size();
    }

    @Unique
    public boolean hasStructureAt(Position position, org.bukkit.generator.structure.Structure structure) {
        Preconditions.checkArgument(position != null, "position cannot be null");
        Preconditions.checkArgument(structure != null, "structure cannot be null");
        BlockPos pos = BlockPos.containing(position.x(), position.y(), position.z());
        return this.getHandle().structureManager()
                .getStructureWithPieceAt(pos, CraftStructure.bukkitToMinecraft(structure))
                .isValid();
    }

    @Unique
    public void sendGameEvent(org.bukkit.entity.Entity sourceEntity, org.bukkit.GameEvent gameEvent, Vector position) {
        Preconditions.checkArgument(gameEvent != null, "gameEvent cannot be null");
        Preconditions.checkArgument(position != null, "position cannot be null");
        net.minecraft.world.level.gameevent.GameEvent nmsEvent =
                org.bukkit.craftbukkit.v1_20_R1.CraftGameEvent.bukkitToMinecraft(gameEvent);
        GameEvent.Context context = GameEvent.Context.of(
                sourceEntity == null ? null : ((CraftEntity) sourceEntity).getHandle());
        this.getHandle().gameEvent(nmsEvent,
                new Vec3(position.getX(), position.getY(), position.getZ()), context);
    }

    @Unique
    public boolean createExplosion(org.bukkit.entity.Entity source, Location loc, float power,
            boolean setFire, boolean breakBlocks, boolean excludeSourceFromDamage) {
        Preconditions.checkArgument(loc != null, "location cannot be null");
        // excludeSourceFromDamage 无 vanilla 对应参数，近似为委托给带源的爆炸
        net.minecraft.world.level.Level.ExplosionInteraction interaction = breakBlocks
                ? net.minecraft.world.level.Level.ExplosionInteraction.TNT
                : net.minecraft.world.level.Level.ExplosionInteraction.NONE;
        this.getHandle().explode(source == null ? null : ((CraftEntity) source).getHandle(),
                loc.getX(), loc.getY(), loc.getZ(), power, setFire, interaction);
        return true;
    }

    @Unique
    public Location findLightningTarget(Location origin) {
        Preconditions.checkArgument(origin != null, "location cannot be null");
        // ServerLevel#findLightningTargetAround 由 AT 加宽（m_143288_）后直访
        BlockPos struck = this.getHandle().findLightningTargetAround(
                BlockPos.containing(origin.x(), origin.y(), origin.z()));
        return new Location((World) (Object) this, struck.getX() + 0.5D, struck.getY(), struck.getZ() + 0.5D);
    }

    @Unique
    public Location findLightningRod(Location origin) {
        Preconditions.checkArgument(origin != null, "location cannot be null");
        ServerLevel level = this.getHandle();
        int topX = BlockPos.containing(origin.x(), origin.y(), origin.z()).getX();
        int topZ = BlockPos.containing(origin.x(), origin.y(), origin.z()).getZ();
        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, topX, topZ);
        // 与 vanilla 引雷一致：在雨面以下向下搜索最多 128 格的避雷针
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 128; i++) {
            cursor.set(topX, topY - i, topZ);
            if (!level.isLoaded(cursor)) {
                break;
            }
            if (level.getBlockState(cursor).is(net.minecraft.world.level.block.Blocks.LIGHTNING_ROD)) {
                return new Location((World) (Object) this, cursor.getX() + 0.5D, cursor.getY(), cursor.getZ() + 0.5D);
            }
        }
        return null;
    }

    @Unique
    public RayTraceResult rayTraceEntities(Position start, Vector direction, double maxDistance, double raySize,
            Predicate<? super org.bukkit.entity.Entity> filter) {
        Preconditions.checkArgument(start != null, "start cannot be null");
        Location startLoc = new Location((World) (Object) this, start.x(), start.y(), start.z());
        return this.rayTraceEntities(startLoc, direction, maxDistance, raySize, filter);
    }

    @Unique
    public RayTraceResult rayTraceBlocks(Position start, Vector direction, double maxDistance,
            FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks, Predicate<? super Block> canCollide) {
        Preconditions.checkArgument(start != null, "start cannot be null");
        Preconditions.checkArgument(canCollide != null, "canCollide cannot be null");
        Vector dirN = direction.clone().normalize();
        Vector cursor = new Vector(start.x(), start.y(), start.z());
        double remaining = maxDistance;
        while (remaining > 1.0E-7D) {
            RayTraceResult hit = this.rayTraceBlocks(
                    cursor.toLocation((World) (Object) this), direction, remaining,
                    fluidCollisionMode, ignorePassableBlocks);
            if (hit == null) {
                return null;
            }
            Block hitBlock = hit.getHitBlock();
            if (hitBlock == null || canCollide.test(hitBlock)) {
                return hit;
            }
            double travelled = hit.getHitPosition().distance(cursor);
            if (travelled <= 1.0E-7D) {
                return null;
            }
            remaining -= travelled;
            cursor = hit.getHitPosition().add(dirN.clone().multiply(1.0E-4D));
        }
        return null;
    }

    @Unique
    public RayTraceResult rayTrace(Position start, Vector direction, double maxDistance,
            FluidCollisionMode fluidCollisionMode, boolean ignorePassableBlocks, double raySize,
            Predicate<? super Block> canCollide, Predicate<? super org.bukkit.entity.Entity> filter) {
        Preconditions.checkArgument(start != null, "start cannot be null");
        Vector origin = new Vector(start.x(), start.y(), start.z());
        RayTraceResult blockHit = this.rayTraceBlocks(start, direction, maxDistance,
                fluidCollisionMode, ignorePassableBlocks, canCollide);
        RayTraceResult entityHit = this.rayTraceEntities(start, direction, maxDistance, raySize, filter);
        if (blockHit == null) {
            return entityHit;
        }
        if (entityHit == null) {
            return blockHit;
        }
        return blockHit.getHitPosition().distanceSquared(origin)
                <= entityHit.getHitPosition().distanceSquared(origin) ? blockHit : entityHit;
    }

    @Unique
    public <T> void spawnParticle(org.bukkit.Particle particle, List<Player> receivers, Player source,
            double x, double y, double z, int count, double offsetX, double offsetY, double offsetZ,
            double extra, T data, boolean forceOverride) {
        if (receivers == null) {
            this.spawnParticle(particle, x, y, z, count, offsetX, offsetY, offsetZ, extra, data, forceOverride);
            return;
        }
        ParticleOptions options = org.bukkit.craftbukkit.v1_20_R1.CraftParticle.toNMS(particle, data);
        ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(
                options, forceOverride, x, y, z,
                (float) offsetX, (float) offsetY, (float) offsetZ, (float) extra, count);
        for (Player receiver : receivers) {
            if (receiver instanceof CraftPlayer craftPlayer && craftPlayer.getWorld() == (Object) this) {
                craftPlayer.getHandle().connection.send(packet);
            }
        }
    }
}
