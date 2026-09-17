package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import com.ixnah.mc.paperarc.bridge.EntityBridge;
import com.ixnah.mc.paperarc.bridge.scheduler.SimpleEntityScheduler;
import io.papermc.paper.entity.TeleportFlag;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Adds Paper's Entity API onto {@link CraftEntity} (batch B25, 37 methods).
 *
 * Compile-time NMS here is vanilla mojang-mappings (loom); members added by
 * Spigot/Paper patches ({@code spawnedViaMobSpawner}, {@code spawnReason},
 * {@code getBukkitYaw()}, {@code freezeLocked}, {@code fixedPose}) only exist
 * at runtime, so they are reached through reflection with graceful defaults.
 * Adventure conversion follows project convention (gson round-trip; PaperAdventure
 * unavailable). Async teleport falls back to sync execution.
 */
@Mixin(CraftEntity.class)
public abstract class CraftEntityApiMixin {

    @Unique
    private static final String PAPERARC_ORIGIN_KEY = "paperarc:origin";
    @Unique
    private static final String PAPERARC_FIXED_POSE_KEY = "paperarc:fixedPose";
    @Unique
    private static final String PAPERARC_FREEZE_LOCKED_KEY = "paperarc:freezeLocked";

    @Shadow
    public abstract Entity getHandle();

    @Shadow
    public abstract org.bukkit.World getWorld();

    @Shadow
    public abstract boolean teleport(Location location, PlayerTeleportEvent.TeleportCause cause);

    // ===== Arclight-only members =====
    // 本文件剩下的唯一一处反射（persistentInvisibility）目标是 Arclight 自己注入到 NMS 上的
    // 字段，不参与 Fabric intermediary 重映射，按 B2-1 的分类保留；其余对 NMS 成员的反射
    // 已全部改成 paperarc.accesswidener 放开后直访。

    // ===== position / rotation getters =====

    @Unique
    public double getX() {
        return this.getHandle().getX();
    }

    @Unique
    public double getY() {
        return this.getHandle().getY();
    }

    @Unique
    public double getZ() {
        return this.getHandle().getZ();
    }

    @Unique
    public float getPitch() {
        return this.getHandle().getXRot();
    }

    @Unique
    public float getYaw() {
        // Paper delegates to getBukkitYaw(), a CraftBukkit-added NMS member that
        // is absent from the vanilla mojmap compile jar -> reflection, falling back
        // to the vanilla yaw accessor.
        try {
            Method m = this.getHandle().getClass().getMethod("getBukkitYaw");
            return (Float) m.invoke(this.getHandle());
        } catch (ReflectiveOperationException e) {
            return this.getHandle().getYRot();
        }
    }

    @Unique
    public String getScoreboardEntryName() {
        return this.getHandle().getScoreboardName();
    }

    // ===== liquid / environment API (Paper Add-entity-liquid-API + powdered snow) =====

    @Unique
    public boolean isInRain() {
        // vanilla 方法是 private，由 paperarc.accesswidener 放开
        return this.getHandle().isInRain();
    }

    @Unique
    public boolean isInBubbleColumn() {
        return this.getHandle().isInBubbleColumn();
    }

    @Unique
    public boolean isInWaterOrBubbleColumn() {
        return this.getHandle().isInWaterOrBubble();
    }

    @Unique
    public boolean isInWaterOrRain() {
        return this.getHandle().isInWaterOrRain();
    }

    @Unique
    public boolean isInWaterOrRainOrBubbleColumn() {
        return this.getHandle().isInWaterRainOrBubble();
    }

    @Unique
    public boolean isInLava() {
        return this.getHandle().isInLava();
    }

    @Unique
    public boolean isUnderWater() {
        return this.getHandle().isUnderWater();
    }

    @Unique
    public boolean isInPowderedSnow() {
        // Paper: either flag may be current depending on tick phase
        Entity handle = this.getHandle();
        return handle.isInPowderSnow || handle.wasInPowderSnow;
    }

    // ===== visibility / sneaking / physics =====

    @Unique
    public boolean isInvisible() {
        return this.getHandle().isInvisible();
    }

    @Unique
    public void setInvisible(boolean invisible) {
        Entity handle = this.getHandle();
        // Paper 还要把这个标志写进 NMS 的 persistentInvisibility，免得 vanilla tick 把它清掉。
        // 该字段由 Arclight 的 EntityMixin 注入（不是 vanilla 成员，不参与重映射），
        // 编译期不可见 -> 保留反射，缺失时静默退化为 vanilla 行为。
        try {
            Field f = Entity.class.getDeclaredField("persistentInvisibility");
            f.setAccessible(true);
            f.setBoolean(handle, invisible);
        } catch (ReflectiveOperationException ignored) {
        }
        handle.setInvisible(invisible);
    }

    @Unique
    public boolean isSneaking() {
        return this.getHandle().isShiftKeyDown();
    }

    @Unique
    public void setSneaking(boolean sneak) {
        this.getHandle().setShiftKeyDown(sneak);
    }

    @Unique
    public boolean hasNoPhysics() {
        return this.getHandle().noPhysics;
    }

    @Unique
    public void setNoPhysics(boolean noPhysics) {
        this.getHandle().noPhysics = noPhysics;
    }

    // ===== pose API =====

    @Unique
    public boolean hasFixedPose() {
        // Paper-added NMS field `fixedPose`; side-map mirror keeps the value
        return ((EntityBridge) this.getHandle()).paper$fixedPose();
    }

    @Unique
    public void setPose(org.bukkit.entity.Pose pose, boolean fixed) {
        Preconditions.checkArgument(pose != null, "pose cannot be null");
        Entity handle = this.getHandle();
        handle.setPose(net.minecraft.world.entity.Pose.valueOf(pose.name()));
        ((EntityBridge) handle).paper$setFixedPose(fixed);
    }

    // ===== freeze tick lock API =====

    @Unique
    public boolean isFreezeTickingLocked() {
        return ((EntityBridge) this.getHandle()).paper$freezeLocked();
    }

    @Unique
    public void lockFreezeTicks(boolean locked) {
        ((EntityBridge) this.getHandle()).paper$setFreezeLocked(locked);
    }

    // ===== spawn metadata / origin / tracking =====

    @Unique
    public boolean fromMobSpawner() {
        // Spigot 的 NMS 字段 spawnedViaMobSpawner 在 Arclight 1.21.1 上**不存在**
        // （vanilla 没有、Arclight 也没注入，已逐个核对），原先的反射恒取不到值。
        // 语义不变地返回默认值，登记在 docs/gaps.md。
        return false;
    }

    @Unique
    public CreatureSpawnEvent.SpawnReason getEntitySpawnReason() {
        // 同 fromMobSpawner()：Spigot 的 NMS 字段 spawnReason 在 Arclight 1.21.1 上不存在。
        return CreatureSpawnEvent.SpawnReason.DEFAULT;
    }

    @Unique
    public Location getOrigin() {
        // Paper 的 Entity-Origin-API：origin 存在 NMS 侧补充字段里（EntityFieldsMixin 注入）
        EntityBridge bridge = (EntityBridge) this.getHandle();
        org.bukkit.util.Vector vec = bridge.getOriginVector();
        if (vec == null) {
            return null;
        }
        java.util.UUID worldId = bridge.getOriginWorld();
        org.bukkit.World world = worldId == null ? null : org.bukkit.Bukkit.getWorld(worldId);
        return new Location(world, vec.getX(), vec.getY(), vec.getZ());
    }

    @Unique
    public boolean isTicking() {
        // Paper 的 NMS Entity#isTicking() 不存在于 vanilla，其实现就是
        // ServerLevel#isPositionEntityTicking(blockPosition())，这里直接内联。
        Entity handle = this.getHandle();
        return handle.level() instanceof ServerLevel level
                && level.isPositionEntityTicking(handle.blockPosition());
    }

    @Unique
    public Set<Player> getTrackedPlayers() {
        Entity handle = this.getHandle();
        if (!(handle.level() instanceof ServerLevel level)) {
            return Collections.emptySet();
        }
        // ChunkMap.entityMap / TrackedEntity.seenBy 都是 vanilla 私有成员，
        // 连同 package-private 的 TrackedEntity 一起由 paperarc.accesswidener 放开
        ChunkMap.TrackedEntity tracker = level.getChunkSource().chunkMap.entityMap.get(handle.getId());
        if (tracker == null) {
            return Collections.emptySet();
        }
        Set<Player> players = new HashSet<>();
        for (ServerPlayerConnection conn : tracker.seenBy) {
            Player bukkit = Bukkit.getPlayer(conn.getPlayer().getUUID());
            if (bukkit != null) {
                players.add(bukkit);
            }
        }
        return players;
    }

    @Unique
    public void broadcastHurtAnimation(Collection<Player> players) {
        Preconditions.checkArgument(players != null, "players cannot be null");
        Preconditions.checkArgument(!players.contains(this), "Cannot broadcast hurt animation to self without a yaw");
        int id = this.getHandle().getId();
        for (Player player : players) {
            if (!(player instanceof CraftPlayer craftPlayer)) {
                continue;
            }
            ServerPlayer handled = craftPlayer.getHandle();
            if (handled.connection != null) {
                // Paper routes through CraftPlayer#sendHurtAnimation(0, entity);
                // equivalent vanilla packet send with zero yaw
                handled.connection.send(new ClientboundHurtAnimationPacket(id, 0.0F));
            }
        }
    }

    // ===== collision API =====

    @Unique
    public boolean collidesAt(Location location) {
        Preconditions.checkArgument(location != null, "location cannot be null");
        Entity handle = this.getHandle();
        // Paper uses getBoundingBoxAt(x,y,z) (Paper-added); equivalent: move the
        // current bounding box by the position delta
        AABB box = handle.getBoundingBox().move(
                location.getX() - handle.getX(),
                location.getY() - handle.getY(),
                location.getZ() - handle.getZ());
        return !handle.level().noCollision(handle, box);
    }

    @Unique
    public boolean wouldCollideUsing(org.bukkit.util.BoundingBox boundingBox) {
        Preconditions.checkArgument(boundingBox != null, "boundingBox cannot be null");
        Entity handle = this.getHandle();
        AABB aabb = new AABB(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ(),
                boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ());
        return !handle.level().noCollision(handle, aabb);
    }

    // ===== spawn / display name =====

    @Unique
    public boolean spawnAt(Location location, CreatureSpawnEvent.SpawnReason reason) {
        Preconditions.checkArgument(location != null, "location cannot be null");
        Preconditions.checkArgument(reason != null, "reason cannot be null");
        if (location.getWorld() == null) {
            return false;
        }
        Entity handle = this.getHandle();
        handle.moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        net.minecraft.world.level.Level level = handle.level();
        // Spigot 的 Level#addFreshEntity(Entity, SpawnReason) 在 Arclight 上叫
        // IWorldWriterBridge#bridge$addEntity —— Arclight 自有成员，名字不参与重映射，
        // 按 B2-1 分类保留反射；拿不到就退回 vanilla 的单参重载（丢失 spawn reason）。
        try {
            Method add = level.getClass().getMethod("bridge$addEntity", Entity.class, CreatureSpawnEvent.SpawnReason.class);
            return (Boolean) add.invoke(level, handle, reason);
        } catch (ReflectiveOperationException e) {
            return level.addFreshEntity(handle);
        }
    }

    @Unique
    public net.kyori.adventure.text.Component teamDisplayName() {
        try {
            net.minecraft.network.chat.Component vanilla = this.getHandle().getDisplayName();
            String json = net.minecraft.network.chat.Component.Serializer.toJson(vanilla,
                    this.getHandle().level().registryAccess());
            // PaperAdventure unavailable: gson round-trip instead
            return net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(json);
        } catch (Exception e) {
            return net.kyori.adventure.text.Component.empty();
        }
    }

    // ===== teleport API =====

    @Unique
    public boolean teleport(Location location, PlayerTeleportEvent.TeleportCause cause,
                            TeleportFlag... flags) {
        Preconditions.checkArgument(location != null, "location cannot be null");
        location.checkFinite();
        Set<TeleportFlag> flagSet = Set.of(flags);
        boolean dismount = !flagSet.contains(TeleportFlag.EntityState.RETAIN_VEHICLE);
        boolean retainPassengers = flagSet.contains(TeleportFlag.EntityState.RETAIN_PASSENGERS);
        Entity handle = this.getHandle();
        // cross-world constraints from Paper's More-Teleport-API
        if ((retainPassengers && handle.isVehicle()) || (!dismount && handle.isPassenger())) {
            if (location.getWorld() != this.getWorld()) {
                return false;
            }
        }
        if (dismount && handle.isPassenger()) {
            handle.stopRiding();
        }
        if (!retainPassengers && handle.isVehicle()) {
            handle.ejectPassengers();
        }
        return teleport(location, cause);
    }

    /**
     * Paper 的 {@code Entity#teleportAsync}。原实现直接
     * {@code completedFuture(teleport(...))} —— 插件从异步线程调用就会**在异步线程上传送实体**。
     * 现在非主线程投递到主线程执行，主线程调用保持同步完成（与 Paper 一致：
     * Paper 在主线程上也是能立即完成就立即完成）。
     *
     * <p>与 Paper 的差异：Arclight 没有 Folia 的"先异步加载目标区块再传送"管线，
     * 目标区块的加载仍然发生在主线程的 {@code teleport} 里。
     */
    @Unique
    public CompletableFuture<Boolean> teleportAsync(Location location, PlayerTeleportEvent.TeleportCause cause,
                                                    TeleportFlag... flags) {
        Preconditions.checkArgument(location != null, "location cannot be null");
        net.minecraft.server.MinecraftServer server = this.getHandle().getServer();
        if (server == null || server.isSameThread()) {
            return CompletableFuture.completedFuture(teleport(location, cause, flags));
        }
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        server.execute(() -> {
            try {
                result.complete(teleport(location, cause, flags));
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        });
        return result;
    }

    // ===== batch blocked-1 addition: EntityScheduler sync-fallback =====

    @Unique
    private io.papermc.paper.threadedregions.scheduler.EntityScheduler PAPERARC_ENTITY_SCHEDULER;

    /**
     * Sync-fallback {@link io.papermc.paper.threadedregions.scheduler.EntityScheduler}:
     * tasks run on the main server thread via the classic Bukkit scheduler; the
     * retired callback fires when the entity is no longer valid at execution
     * time. NOT truly asynchronous / Folia region-based.
     */
    @Unique
    public io.papermc.paper.threadedregions.scheduler.EntityScheduler getScheduler() {
        io.papermc.paper.threadedregions.scheduler.EntityScheduler scheduler = this.PAPERARC_ENTITY_SCHEDULER;
        if (scheduler == null) {
            scheduler = new SimpleEntityScheduler((org.bukkit.entity.Entity) (Object) this);
            this.PAPERARC_ENTITY_SCHEDULER = scheduler;
        }
        return scheduler;
    }

    /**
     * paper-api 的 {@code CommandSender.name()}（{@code Entity extends CommandSender}）。
     * Paper 返回 {@code Entity#getName()} 的 adventure 形态 —— 有自定义名用自定义名，
     * 否则用实体类型的翻译名。此前只有 CraftCommandBlock 有实现体，控制台/实体/玩家
     * 上调用即 AbstractMethodError。
     */
    @Unique
    public net.kyori.adventure.text.Component name() {
        net.minecraft.network.chat.Component vanilla = this.getHandle().getName();
        if (vanilla == null) {
            return net.kyori.adventure.text.Component.empty();
        }
        return net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(
                net.minecraft.network.chat.Component.Serializer.toJson(
                        vanilla, this.getHandle().level().registryAccess()));
    }

    /**
     * paper-api 的 {@code Entity extends HoverEventSource<HoverEvent.ShowEntity>}。
     * 实体 key 走 NMS 注册表（{@code BuiltInRegistries.ENTITY_TYPE}）而不是 Bukkit
     * {@code EntityType}，这样模组实体也能取到 key。
     */
    @Unique
    public net.kyori.adventure.text.event.HoverEvent<net.kyori.adventure.text.event.HoverEvent.ShowEntity>
            asHoverEvent(java.util.function.UnaryOperator<net.kyori.adventure.text.event.HoverEvent.ShowEntity> op) {
        net.minecraft.world.entity.Entity handle = this.getHandle();
        net.minecraft.resources.ResourceLocation id =
                net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(handle.getType());
        net.kyori.adventure.text.event.HoverEvent.ShowEntity show =
                net.kyori.adventure.text.event.HoverEvent.ShowEntity.of(
                        net.kyori.adventure.key.Key.key(id.getNamespace(), id.getPath()),
                        handle.getUUID(), this.name());
        return net.kyori.adventure.text.event.HoverEvent.showEntity(op == null ? show : op.apply(show));
    }

    // ===== B2-4：Nameable 的 adventure 版（paper） =====

    @Unique
    public net.kyori.adventure.text.Component customName() {
        String legacy = ((org.bukkit.entity.Entity) (Object) this).getCustomName();
        return legacy == null ? null
                : net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                        .legacySection().deserialize(legacy);
    }

    @Unique
    public void customName(net.kyori.adventure.text.Component customName) {
        ((org.bukkit.entity.Entity) (Object) this).setCustomName(customName == null ? null
                : net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                        .legacySection().serialize(customName));
    }
}
