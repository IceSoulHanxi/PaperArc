package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Entity} (generated, trimmed for 1.20.1).
 * Adds 30 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 *
 * <p>paper-api 的 {@code Entity} 还多两个父接口 —— {@code HoverEventSource<ShowEntity>}
 * （抽象方法 {@code asHoverEvent}，实现在 CraftEntityApiMixin）与 {@code Sound.Emitter}
 * （纯标记接口，用于 {@code Audience.playSound(Sound, Emitter)}）。Mixin 会把这里声明的
 * 父接口合并到接口目标上（见 docs/execution-plan-2026-09-16.md A2-1）。
 */
@Mixin(targets = "org.bukkit.entity.Entity", remap = false)
public interface EntityIfaceMixin extends
        net.kyori.adventure.text.event.HoverEventSource<net.kyori.adventure.text.event.HoverEvent.ShowEntity>,
        net.kyori.adventure.sound.Sound.Emitter {

    // paper-api 的 Entity.teleportAsync 只有两个 default 方法（1.20.1 没有抽象形态），
    // 运行时接口里没有 —— 插件调 entity.teleportAsync(loc) 直接 NoSuchMethodError。
    // 方法体照抄 paper-api 的 default 实现：先异步取目标区块，再在回调（主线程）里同步传送。

    @Unique
    public default java.util.concurrent.CompletableFuture<java.lang.Boolean> teleportAsync(org.bukkit.Location loc) {
        return teleportAsync(loc, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    @Unique
    public default java.util.concurrent.CompletableFuture<java.lang.Boolean> teleportAsync(
            org.bukkit.Location loc, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause cause) {
        java.util.concurrent.CompletableFuture<java.lang.Boolean> future = new java.util.concurrent.CompletableFuture<>();
        loc.getWorld().getChunkAtAsync(loc)
                .thenAccept(chunk -> future.complete(((org.bukkit.entity.Entity) this).teleport(loc, cause)))
                .exceptionally(ex -> {
                    future.completeExceptionally(ex);
                    return null;
                });
        return future;
    }

    @Unique
    public abstract boolean isFreezeTickingLocked();

    @Unique
    public abstract void lockFreezeTicks(boolean p0);

    @Unique
    public abstract boolean isSneaking();

    @Unique
    public abstract void setSneaking(boolean p0);

    @Unique
    public abstract void setPose(org.bukkit.entity.Pose p0, boolean p1);

    @Unique
    public abstract boolean hasFixedPose();

    @Unique
    public abstract net.kyori.adventure.text.Component teamDisplayName();

    @Unique
    public abstract org.bukkit.Location getOrigin();

    @Unique
    public abstract boolean fromMobSpawner();

    @Unique
    public abstract org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason getEntitySpawnReason();

    @Unique
    public abstract boolean isUnderWater();

    @Unique
    public abstract boolean isInRain();

    @Unique
    public abstract boolean isInBubbleColumn();

    @Unique
    public abstract boolean isInWaterOrRain();

    @Unique
    public abstract boolean isInWaterOrBubbleColumn();

    @Unique
    public abstract boolean isInWaterOrRainOrBubbleColumn();

    @Unique
    public abstract boolean isInLava();

    @Unique
    public abstract boolean isTicking();

    @Unique
    public abstract java.util.Set getTrackedPlayers();

    @Unique
    public abstract boolean spawnAt(org.bukkit.Location p0, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason p1);

    @Unique
    public abstract boolean isInPowderedSnow();

    @Unique
    public abstract double getX();

    @Unique
    public abstract double getY();

    @Unique
    public abstract double getZ();

    @Unique
    public abstract float getPitch();

    @Unique
    public abstract float getYaw();

    @Unique
    public abstract boolean collidesAt(org.bukkit.Location p0);

    @Unique
    public abstract boolean wouldCollideUsing(org.bukkit.util.BoundingBox p0);

    @Unique
    public abstract io.papermc.paper.threadedregions.scheduler.EntityScheduler getScheduler();

    @Unique
    public abstract java.lang.String getScoreboardEntryName();

    @Unique
    public abstract boolean teleport(org.bukkit.Location p0, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause p1, io.papermc.paper.entity.TeleportFlag... p2);
    // ===== A4-4：paper-api 的 default 方法体照搬（运行时接口里一个都没有）=====

    @Unique
    public default org.bukkit.Chunk getChunk() {
        return ((org.bukkit.entity.Entity) this).getLocation().getChunk();
    }

    @Unique
    public default void setPose(org.bukkit.entity.Pose pose) {
        setPose(pose, false);
    }

    @Unique
    public default boolean spawnAt(org.bukkit.Location location) {
        return spawnAt(location, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Unique
    public default boolean teleport(org.bukkit.Location location, io.papermc.paper.entity.TeleportFlag... flags) {
        // 偏差：Arclight 没有 Paper 的 TeleportFlag 传送管线，退化为普通 teleport（PLUGIN 原因）
        return ((org.bukkit.entity.Entity) this).teleport(location,
                org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    /** 实现体在 CraftEntityApiMixin 上（HoverEventSource 的终端方法）。 */
    @Unique
    public abstract net.kyori.adventure.text.event.HoverEvent<net.kyori.adventure.text.event.HoverEvent.ShowEntity> asHoverEvent(java.util.function.UnaryOperator<net.kyori.adventure.text.event.HoverEvent.ShowEntity> op);

}
