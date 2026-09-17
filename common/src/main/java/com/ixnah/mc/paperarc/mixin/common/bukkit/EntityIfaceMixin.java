package com.ixnah.mc.paperarc.mixin.common.bukkit;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Entity} (generated).
 * Adds 35 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 *
 * <p>paper-api 的 {@code Entity extends HoverEventSource<HoverEvent.ShowEntity>, Sound.Emitter}；
 * Mixin 会把 mixin 自身的父接口合并到接口目标上（B4b，Phase A2-1 已实测）。
 * {@code Sound.Emitter} 无抽象方法，纯标记；{@code asHoverEvent} 是 paper 在
 * {@code Entity} 上给的 default 实现，照抄在下面。
 */
@Mixin(targets = "org.bukkit.entity.Entity", remap = false)
public interface EntityIfaceMixin extends HoverEventSource<HoverEvent.ShowEntity>, Sound.Emitter {

    /**
     * paper-api {@code Entity#asHoverEvent} 的 default 方法体。
     *
     * <p>与 paper 的唯一差别：paper 的 {@code NamespacedKey} 自己实现了 adventure 的
     * {@code Key}，可以直接塞进 {@code ShowEntity.of}；Arclight 运行时的
     * {@code NamespacedKey} 没有这层继承（是 final class，补不了），所以就地转成
     * {@code Key.key(namespace, key)}。
     */
    @Unique
    public default HoverEvent<HoverEvent.ShowEntity> asHoverEvent(
            java.util.function.UnaryOperator<HoverEvent.ShowEntity> op) {
        org.bukkit.entity.Entity self = (org.bukkit.entity.Entity) this;
        org.bukkit.NamespacedKey type = self.getType().getKey();
        return HoverEvent.showEntity(op.apply(HoverEvent.ShowEntity.of(
                net.kyori.adventure.key.Key.key(type.getNamespace(), type.getKey()),
                self.getUniqueId(), self.customName())));
    }

    @Unique
    public abstract void setInvisible(boolean p0);

    @Unique
    public abstract boolean isInvisible();

    @Unique
    public abstract void setNoPhysics(boolean p0);

    @Unique
    public abstract boolean hasNoPhysics();

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
    public abstract void broadcastHurtAnimation(java.util.Collection p0);

    @Unique
    public abstract boolean teleport(org.bukkit.Location p0, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause p1, io.papermc.paper.entity.TeleportFlag[] p2);

    @Unique
    public abstract java.util.concurrent.CompletableFuture<java.lang.Boolean> teleportAsync(org.bukkit.Location p0, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause p1, io.papermc.paper.entity.TeleportFlag[] p2);

    // paper-api 的 teleportAsync 另外两个重载是 default 方法，运行时接口里没有 ——
    // 插件调 entity.teleportAsync(loc) 直接 NoSuchMethodError。方法体照抄 paper-api：
    // 先异步取目标区块，再在回调（主线程）里同步传送。
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
}
