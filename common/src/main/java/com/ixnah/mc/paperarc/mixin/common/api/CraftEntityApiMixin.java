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
    public Location getOrigin() {
        // Paper stores origin in NMS fields (Entity-Origin-API.patch) injected by
        // EntityFieldsMixin and reached through EntityBridge
        EntityBridge bridge = (EntityBridge) this.getHandle();
        org.bukkit.util.Vector origin = bridge.getOriginVector();
        if (origin == null) {
            return null;
        }
        org.bukkit.World world = null;
        if (bridge.getOriginWorld() != null) {
            world = org.bukkit.Bukkit.getWorld(bridge.getOriginWorld());
        }
        return origin.toLocation(world);
    }

    @Shadow
    public abstract Entity getHandle();

    @Shadow
    public abstract org.bukkit.World getWorld();

    @Shadow
    public abstract boolean teleport(Location location, PlayerTeleportEvent.TeleportCause cause);

    // ===== shared reflection helpers =====
    // 反射目标只剩 Arclight 注入在 NMS Entity 上的 persistentInvisibility（EntityMixin），
    // 这类成员不参与 srg 重映射，按字面名查找在运行时正确；NMS 自身成员一律走 AT/@Accessor。

    @Unique
    private static Field paperarc$field(Class<?> owner, String name) {
        try {
            Field f = owner.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @Unique
    private static boolean paperarc$setBoolField(Entity handle, String name, boolean value) {
        Field f = paperarc$field(Entity.class, name);
        if (f == null) {
            return false;
        }
        try {
            f.setBoolean(handle, value);
            return true;
        } catch (IllegalAccessException e) {
            return false;
        }
    }

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
            // CraftBukkit 侧成员（NMS 类上由 CB 补丁添加，不参与 srg 重映射），按字面名反射在运行时正确。
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
        // vanilla private 方法由 AT 加宽（m_20285_）后直访
        return this.getHandle().isInRain();
    }

    @Unique
    public boolean isInBubbleColumn() {
        // vanilla private 方法由 AT 加宽（m_20305_）后直访
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
        // Paper also persists the flag in NMS persistentInvisibility so the
        // vanilla tick does not clear it; field exists at runtime -> reflection
        paperarc$setBoolField(handle, "persistentInvisibility", invisible);
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
        return ((com.ixnah.mc.paperarc.bridge.EntityBridge) this.getHandle()).paper$fixedPose();
    }

    @Unique
    public void setPose(org.bukkit.entity.Pose pose, boolean fixed) {
        Preconditions.checkArgument(pose != null, "pose cannot be null");
        Entity handle = this.getHandle();
        com.ixnah.mc.paperarc.bridge.EntityBridge bridge =
                (com.ixnah.mc.paperarc.bridge.EntityBridge) handle;
        bridge.paper$setFixedPose(false);
        handle.setPose(net.minecraft.world.entity.Pose.valueOf(pose.name()));
        bridge.paper$setFixedPose(fixed);
    }

    // ===== freeze tick lock API =====

    @Unique
    public boolean isFreezeTickingLocked() {
        return ((com.ixnah.mc.paperarc.bridge.EntityBridge) this.getHandle()).paper$freezeLocked();
    }

    @Unique
    public void lockFreezeTicks(boolean locked) {
        ((com.ixnah.mc.paperarc.bridge.EntityBridge) this.getHandle()).paper$setFreezeLocked(locked);
    }


    // ===== spawn metadata / origin / tracking =====

    @Unique
    public boolean fromMobSpawner() {
        // A8/Y-3（E6）：字段由 EntityFieldsMixin 注入、ServerLevelSpawnReasonMixin 写入、
        // Entity#saveWithoutId/load 落盘，不再是恒定的默认值。
        return ((com.ixnah.mc.paperarc.bridge.EntityBridge) this.getHandle()).paper$spawnedViaMobSpawner();
    }

    @Unique
    public CreatureSpawnEvent.SpawnReason getEntitySpawnReason() {
        CreatureSpawnEvent.SpawnReason reason =
                ((com.ixnah.mc.paperarc.bridge.EntityBridge) this.getHandle()).paper$spawnReason();
        return reason == null ? CreatureSpawnEvent.SpawnReason.DEFAULT : reason;
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
        // ChunkMap.entityMap 由 AT 加宽（f_140150_）；TrackedEntity.seenBy 由 AT 加宽（f_140475_）
        Int2ObjectMap<?> trackers = level.getChunkSource().chunkMap.entityMap;
        Object tracker = trackers == null ? null : trackers.get(handle.getId());
        if (tracker == null) {
            return Collections.emptySet();
        }
        Set<Player> players = new HashSet<>();
        for (Object conn : (Collection<?>) ((net.minecraft.server.level.ChunkMap.TrackedEntity) tracker).seenBy) {
            ServerPlayer tracked = ((ServerPlayerConnection) conn).getPlayer();
            Player bukkit = Bukkit.getPlayer(tracked.getUUID());
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
        // A8/Y-3：reason 现在真的存得下（EntityFieldsMixin 的 spawnReason 字段）。
        ((com.ixnah.mc.paperarc.bridge.EntityBridge) handle).paper$setSpawnReason(reason);
        net.minecraft.world.level.Level level = handle.level();
        try {
            // Spigot patches addFreshEntity(Entity, SpawnReason) onto Level;
            // absent from the vanilla compile jar -> reflection with plain fallback
            Method add = level.getClass().getMethod("addFreshEntity", Entity.class, CreatureSpawnEvent.SpawnReason.class);
            return (Boolean) add.invoke(level, handle, reason);
        } catch (ReflectiveOperationException e) {
            return level.addFreshEntity(handle);
        }
    }

    /**
     * paper-api 的 {@code CommandSender.name()}（{@code Entity extends CommandSender}）。
     * Paper 返回 {@code Entity#getName()} 的 adventure 形态 —— 有自定义名用自定义名，
     * 否则用实体类型的翻译名。
     */
    @Unique
    public net.kyori.adventure.text.Component name() {
        net.minecraft.network.chat.Component vanilla = this.getHandle().getName();
        return vanilla == null ? net.kyori.adventure.text.Component.empty()
                : net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(
                        net.minecraft.network.chat.Component.Serializer.toJson(vanilla));
    }

    /**
     * paper-api 的 {@code Entity extends HoverEventSource<HoverEvent.ShowEntity>}。
     * 实体 key 走 NMS 注册表（{@code BuiltInRegistries.ENTITY_TYPE}）而不是 Bukkit
     * {@code EntityType}，避免模组实体在 Bukkit 侧映射不到常量。
     */
    @Unique
    public net.kyori.adventure.text.event.HoverEvent<net.kyori.adventure.text.event.HoverEvent.ShowEntity> asHoverEvent(java.util.function.UnaryOperator<net.kyori.adventure.text.event.HoverEvent.ShowEntity> op) {
        Entity handle = this.getHandle();
        net.minecraft.resources.ResourceLocation id =
                net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(handle.getType());
        net.kyori.adventure.text.event.HoverEvent.ShowEntity show =
                net.kyori.adventure.text.event.HoverEvent.ShowEntity.of(
                        net.kyori.adventure.key.Key.key(id.getNamespace(), id.getPath()),
                        handle.getUUID(), this.customName());
        return net.kyori.adventure.text.event.HoverEvent.showEntity(op == null ? show : op.apply(show));
    }

    @Unique
    public net.kyori.adventure.text.Component teamDisplayName() {
        try {
            net.minecraft.network.chat.Component vanilla = this.getHandle().getDisplayName();
            String json = net.minecraft.network.chat.Component.Serializer.toJson(vanilla);
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
     * Paper's {@code org.bukkit.Nameable} adventure API ({@code Entity} extends
     * {@code Nameable}): delegates to {@code Entity#getCustomName()} / {@code setCustomName(Component)}
     * with gson round-trip between adventure and NMS components.
     */
    @Unique
    public net.kyori.adventure.text.Component customName() {
        net.minecraft.network.chat.Component nms = this.getHandle().getCustomName();
        return nms == null ? null
                : net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().deserialize(
                        net.minecraft.network.chat.Component.Serializer.toJson(nms));
    }

    @Unique
    public void customName(net.kyori.adventure.text.Component customName) {
        this.getHandle().setCustomName(customName == null ? null
                : net.minecraft.network.chat.Component.Serializer.fromJson(
                        net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(customName)));
    }
    // ===== io.papermc.paper.entity.Frictional（A4-3 父接口差集）=====
    // Item 与 LivingEntity 两个接口都 extends Frictional，公共宿主是 CraftEntity。
    // 偏差：这里只保存/回读状态，没有接进 NMS 的移动摩擦计算（Paper 是在
    // LivingEntity#travel / Entity#move 里按该状态改 friction）；已记 docs/gaps.md。

    @Unique
    public net.kyori.adventure.util.TriState getFrictionState() {
        return ((com.ixnah.mc.paperarc.bridge.EntityBridge) this.getHandle()).paper$frictionState();
    }

    @Unique
    public void setFrictionState(net.kyori.adventure.util.TriState state) {
        com.google.common.base.Preconditions.checkArgument(state != null, "state cannot be null");
        ((com.ixnah.mc.paperarc.bridge.EntityBridge) this.getHandle()).paper$setFrictionState(state);
    }

}
