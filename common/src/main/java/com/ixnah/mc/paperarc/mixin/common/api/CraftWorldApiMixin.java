package com.ixnah.mc.paperarc.mixin.common.api;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import com.google.common.base.Preconditions;

import io.papermc.paper.math.Position;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
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
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v.CraftGameEvent;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.bukkit.craftbukkit.v.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v.CraftParticle;
import org.bukkit.craftbukkit.v.CraftRaid;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.bukkit.craftbukkit.v.generator.structure.CraftStructure;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;

/**
 * Adds Paper's CraftWorld APIs missing from Arclight's Spigot CraftBukkit.
 * Batch B24 (docs/api-slices/B24.json).
 */
@Mixin(CraftWorld.class)
public abstract class CraftWorldApiMixin {

    /** Paper 侧补充状态（原 ApiState 副表键 "sendViewDistance"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private Integer paperarc$sendViewDistance;

    /** Paper 侧补充状态（原 ApiState 副表键 "viewDistance"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private Integer paperarc$viewDistance;

    /** Paper 侧补充状态（原 ApiState 副表键 "simulationDistance"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private Integer paperarc$simulationDistance;

    /** Paper 侧补充状态（原 ApiState 副表键 "voidDamageEnabled"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private Boolean paperarc$voidDamageEnabled;

    /** Paper 侧补充状态（原 ApiState 副表键 "voidDamageAmount"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private Float paperarc$voidDamageAmount;

    /** Paper 侧补充状态（原 ApiState 副表键 "voidDamageMinBuildHeightOffset"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private Double paperarc$voidDamageMinBuildHeightOffset;

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
    public boolean isFixedTime() {
        return this.getHandle().dimensionType().hasFixedTime();
    }

    @Unique
    public boolean isDayTime() {
        return this.getHandle().isBrightOutside();
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
                this.getHandle().registryAccess().lookupOrThrow(Registries.BLOCK);
        Collection<Material> materials = new ArrayList<>();
        blocks.get(this.getHandle().dimensionType().infiniburn())
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
        return nms == null ? null : new CraftRaid(nms, this.getHandle());
    }

    /**
     * Paper 的 {@code World#getChunkAtAsync}。原实现是
     * {@code completedFuture(getChunkAt(...))} —— 插件从异步线程调用时会**在异步线程上
     * 加载区块**（世界数据损坏 / CME），是真 bug 而不只是行为差异。
     *
     * <p>现在：非主线程一律 {@code server.execute(...)} 投递到主线程，future 在主线程完成，
     * 因此 {@code thenAccept} 之类的回调也落在主线程 —— 与 Paper 语义一致。
     *
     * <p>与 Paper 的差异（已知并接受）：
     * <ul>
     *   <li>主线程调用时仍然是**同步加载**（Paper 不阻塞）。**不要**改用
     *       {@code ServerChunkCache#getChunkFuture}：它在主线程分支里会
     *       {@code mainThreadProcessor.managedBlock(future::isDone)} —— 一样阻塞，而且会在
     *       主线程任务里**重入** {@code runDistanceManagerUpdates}；1.20.1 真机实测该写法会把
     *       票据队列写坏（{@code LongLinkedOpenHashSet.shiftKeys} 抛 AIOOBE，
     *       服务器 "Exception ticking world" 崩溃）。Arclight 没有 Paper 的异步区块加载管线。</li>
     *   <li>{@code urgent} 被忽略：没有加载优先级队列。</li>
     * </ul>
     */
    @Unique
    public CompletableFuture<org.bukkit.Chunk> getChunkAtAsync(int x, int z, boolean gen, boolean urgent) {
        net.minecraft.server.MinecraftServer server = this.getHandle().getServer();
        if (server == null || server.isSameThread()) {
            return CompletableFuture.completedFuture(this.getChunkAt(x, z, gen));
        }
        CompletableFuture<org.bukkit.Chunk> result = new CompletableFuture<>();
        server.execute(() -> {
            try {
                result.complete(this.getChunkAt(x, z, gen));
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result;
    }

    @Unique
    public int getSendViewDistance() {
        return (this.paperarc$sendViewDistance != null ? this.paperarc$sendViewDistance : (this.getViewDistance() + 1));
    }

    @Unique
    public void setSendViewDistance(int sendViewDistance) {
        Preconditions.checkArgument(sendViewDistance >= -1, "sendViewDistance must be >= -1");
        this.paperarc$sendViewDistance = sendViewDistance;
    }

    @Unique
    public void setViewDistance(int viewDistance) {
        // vanilla 视距是服务器全局的；这里仅记录每世界覆写值
        Preconditions.checkArgument(viewDistance >= -1, "viewDistance must be >= -1");
        this.paperarc$viewDistance = viewDistance;
    }

    @Unique
    public void setSimulationDistance(int simulationDistance) {
        Preconditions.checkArgument(simulationDistance >= -1, "simulationDistance must be >= -1");
        this.paperarc$simulationDistance = simulationDistance;
    }

    @Unique
    public boolean isVoidDamageEnabled() {
        return (this.paperarc$voidDamageEnabled != null ? this.paperarc$voidDamageEnabled : (Boolean.TRUE));
    }

    @Unique
    public void setVoidDamageEnabled(boolean enabled) {
        this.paperarc$voidDamageEnabled = enabled;
    }

    @Unique
    public float getVoidDamageAmount() {
        return (this.paperarc$voidDamageAmount != null ? this.paperarc$voidDamageAmount : (4.0F));
    }

    @Unique
    public void setVoidDamageAmount(float amount) {
        Preconditions.checkArgument(amount >= 0.0F, "amount must be >= 0");
        this.paperarc$voidDamageAmount = amount;
    }

    @Unique
    public double getVoidDamageMinBuildHeightOffset() {
        return (this.paperarc$voidDamageMinBuildHeightOffset != null ? this.paperarc$voidDamageMinBuildHeightOffset : (0.0D));
    }

    @Unique
    public void setVoidDamageMinBuildHeightOffset(double offset) {
        this.paperarc$voidDamageMinBuildHeightOffset = offset;
    }

    @Unique
    public int getTileEntityCount() {
        // ChunkMap#visibleChunkMap 是 private，由 paperarc.accesswidener 放开（同 CraftWorld#getLoadedChunks）
        int count = 0;
        for (ChunkHolder holder : this.getHandle().getChunkSource().chunkMap.visibleChunkMap.values()) {
            net.minecraft.world.level.chunk.LevelChunk chunk = holder.getTickingChunk();
            if (chunk != null) {
                count += chunk.getBlockEntitiesPos().size();
            }
        }
        return count;
    }

    @Unique
    public int getTickableTileEntityCount() {
        // Level#blockEntityTickers 是 protected final，由 paperarc.accesswidener 放开
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
        net.minecraft.core.Holder<net.minecraft.world.level.gameevent.GameEvent> nmsEvent =
                net.minecraft.core.Holder.direct(CraftGameEvent.bukkitToMinecraft(gameEvent));
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
        // ServerLevel#findLightningTargetAround 是 protected，由 paperarc.accesswidener 放开。
        // 注意用 getX/getY/getZ：paper 让 Location 实现了 FinePosition（有 x()/y()/z()），
        // 但 Arclight 1.21.1 的 Location 没有，调 x() 即 NoSuchMethodError（真机实测）。
        BlockPos struck = this.getHandle().findLightningTargetAround(
                BlockPos.containing(origin.getX(), origin.getY(), origin.getZ()));
        return new Location((World) (Object) this, struck.getX() + 0.5D, struck.getY(), struck.getZ() + 0.5D);
    }

    @Unique
    public Location findLightningRod(Location origin) {
        Preconditions.checkArgument(origin != null, "location cannot be null");
        ServerLevel level = this.getHandle();
        // 同 findLightningTarget：Location 上没有 paper 的 x()/y()/z()
        int topX = BlockPos.containing(origin.getX(), origin.getY(), origin.getZ()).getX();
        int topZ = BlockPos.containing(origin.getX(), origin.getY(), origin.getZ()).getZ();
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
        ParticleOptions options = CraftParticle.createParticleParam(particle, data);
        ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(
                options, forceOverride, false, x, y, z,
                (float) offsetX, (float) offsetY, (float) offsetZ, (float) extra, count);
        for (Player receiver : receivers) {
            if (receiver instanceof CraftPlayer craftPlayer && craftPlayer.getWorld() == (Object) this) {
                craftPlayer.getHandle().connection.send(packet);
            }
        }
    }
}
