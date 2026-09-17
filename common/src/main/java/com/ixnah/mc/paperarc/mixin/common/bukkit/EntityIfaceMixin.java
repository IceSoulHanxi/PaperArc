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
}
