package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import com.ixnah.mc.paperarc.bridge.ApiState;
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

    // ===== shared reflection helpers =====

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
    private static boolean paperarc$getBoolField(Entity handle, String name, boolean def) {
        Field f = paperarc$field(Entity.class, name);
        if (f == null) {
            return def;
        }
        try {
            return f.getBoolean(handle);
        } catch (IllegalAccessException e) {
            return def;
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

    @Unique
    private static Object paperarc$invoke(Entity handle, String name, Object... args) {
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = args[i] == null ? Object.class : args[i].getClass();
        }
        try {
            Method m = Entity.class.getDeclaredMethod(name, types);
            m.setAccessible(true);
            return m.invoke(handle, args);
        } catch (ReflectiveOperationException e) {
            return null;
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
        // vanilla method is private -> reflection, false when unavailable
        Object r = paperarc$invoke(this.getHandle(), "isInRain");
        return r instanceof Boolean b && b;
    }

    @Unique
    public boolean isInBubbleColumn() {
        Object r = paperarc$invoke(this.getHandle(), "isInBubbleColumn");
        return r instanceof Boolean b && b;
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
        // Paper-added NMS field `fixedPose`; side-map mirror keeps the value
        // when the runtime field is unavailable
        if (paperarc$field(Entity.class, "fixedPose") != null) {
            return paperarc$getBoolField(this.getHandle(), "fixedPose", false);
        }
        return ApiState.get(this.getHandle(), PAPERARC_FIXED_POSE_KEY, Boolean.FALSE);
    }

    @Unique
    public void setPose(org.bukkit.entity.Pose pose, boolean fixed) {
        Preconditions.checkArgument(pose != null, "pose cannot be null");
        Entity handle = this.getHandle();
        handle.setPose(net.minecraft.world.entity.Pose.valueOf(pose.name()));
        ApiState.put(handle, PAPERARC_FIXED_POSE_KEY, fixed);
        paperarc$setBoolField(handle, "fixedPose", fixed);
    }

    // ===== freeze tick lock API =====

    @Unique
    public boolean isFreezeTickingLocked() {
        // Paper-added NMS field `freezeLocked`; side-map mirror as fallback
        if (paperarc$field(Entity.class, "freezeLocked") != null) {
            return paperarc$getBoolField(this.getHandle(), "freezeLocked", false);
        }
        return ApiState.get(this.getHandle(), PAPERARC_FREEZE_LOCKED_KEY, Boolean.FALSE);
    }

    @Unique
    public void lockFreezeTicks(boolean locked) {
        Entity handle = this.getHandle();
        ApiState.put(handle, PAPERARC_FREEZE_LOCKED_KEY, locked);
        paperarc$setBoolField(handle, "freezeLocked", locked);
    }


    // ===== spawn metadata / origin / tracking =====

    @Unique
    public boolean fromMobSpawner() {
        // Spigot-added NMS field `spawnedViaMobSpawner`; absent from the vanilla
        // compile jar -> reflection, default false
        return paperarc$getBoolField(this.getHandle(), "spawnedViaMobSpawner", false);
    }

    @Unique
    public CreatureSpawnEvent.SpawnReason getEntitySpawnReason() {
        // Spigot-added NMS field `spawnReason`
        Field f = paperarc$field(Entity.class, "spawnReason");
        if (f != null) {
            try {
                Object v = f.get(this.getHandle());
                if (v instanceof CreatureSpawnEvent.SpawnReason reason) {
                    return reason;
                }
            } catch (IllegalAccessException ignored) {
            }
        }
        return CreatureSpawnEvent.SpawnReason.DEFAULT;
    }

    @Unique
    public Location getOrigin() {
        // Paper stores origin in NMS fields added by their patch; no such storage
        // here -> side-map keyed by the NMS handle, null when unset
        return ApiState.get(this.getHandle(), PAPERARC_ORIGIN_KEY, null);
    }

    @Unique
    public boolean isTicking() {
        Object r = paperarc$invoke(this.getHandle(), "isTicking");
        return r instanceof Boolean b && b;
    }

    @Unique
    public Set<Player> getTrackedPlayers() {
        Entity handle = this.getHandle();
        if (!(handle.level() instanceof ServerLevel level)) {
            return Collections.emptySet();
        }
        Field entityMapField = paperarc$field(ChunkMap.class, "entityMap");
        if (entityMapField == null) {
            return Collections.emptySet();
        }
        try {
            Int2ObjectMap<?> trackers = (Int2ObjectMap<?>) entityMapField.get(level.getChunkSource().chunkMap);
            Object tracker = trackers == null ? null : trackers.get(handle.getId());
            if (tracker == null) {
                return Collections.emptySet();
            }
            // ChunkMap.TrackedEntity is package-private -> locate seenBy reflectively
            Field seenByField = paperarc$field(tracker.getClass(), "seenBy");
            if (seenByField == null) {
                return Collections.emptySet();
            }
            Set<Player> players = new HashSet<>();
            for (Object conn : (Collection<?>) seenByField.get(tracker)) {
                ServerPlayer tracked = ((ServerPlayerConnection) conn).getPlayer();
                Player bukkit = Bukkit.getPlayer(tracked.getUUID());
                if (bukkit != null) {
                    players.add(bukkit);
                }
            }
            return players;
        } catch (IllegalAccessException | ClassCastException e) {
            return Collections.emptySet();
        }
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
        // keep the spawn-reason API consistent for the added entity
        Field reasonField = paperarc$field(Entity.class, "spawnReason");
        if (reasonField != null) {
            try {
                reasonField.set(handle, reason);
            } catch (IllegalAccessException ignored) {
            }
        }
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

    @Unique
    public CompletableFuture<Boolean> teleportAsync(Location location, PlayerTeleportEvent.TeleportCause cause,
                                                    TeleportFlag... flags) {
        // sync-fallback: Arclight has no Folia chunk-load-on-move pipeline
        return CompletableFuture.completedFuture(teleport(location, cause, flags));
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
}
