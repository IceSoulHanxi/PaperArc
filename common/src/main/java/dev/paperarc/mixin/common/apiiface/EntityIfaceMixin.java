package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Entity} (generated).
 * Adds 35 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Entity", remap = false)
public interface EntityIfaceMixin {

    public abstract void setInvisible(boolean p0);

    public abstract boolean isInvisible();

    public abstract void setNoPhysics(boolean p0);

    public abstract boolean hasNoPhysics();

    public abstract boolean isFreezeTickingLocked();

    public abstract void lockFreezeTicks(boolean p0);

    public abstract boolean isSneaking();

    public abstract void setSneaking(boolean p0);

    public abstract void setPose(org.bukkit.entity.Pose p0, boolean p1);

    public abstract boolean hasFixedPose();

    public abstract net.kyori.adventure.text.Component teamDisplayName();

    public abstract org.bukkit.Location getOrigin();

    public abstract boolean fromMobSpawner();

    public abstract org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason getEntitySpawnReason();

    public abstract boolean isUnderWater();

    public abstract boolean isInRain();

    public abstract boolean isInBubbleColumn();

    public abstract boolean isInWaterOrRain();

    public abstract boolean isInWaterOrBubbleColumn();

    public abstract boolean isInWaterOrRainOrBubbleColumn();

    public abstract boolean isInLava();

    public abstract boolean isTicking();

    public abstract java.util.Set getTrackedPlayers();

    public abstract boolean spawnAt(org.bukkit.Location p0, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason p1);

    public abstract boolean isInPowderedSnow();

    public abstract double getX();

    public abstract double getY();

    public abstract double getZ();

    public abstract float getPitch();

    public abstract float getYaw();

    public abstract boolean collidesAt(org.bukkit.Location p0);

    public abstract boolean wouldCollideUsing(org.bukkit.util.BoundingBox p0);

    public abstract io.papermc.paper.threadedregions.scheduler.EntityScheduler getScheduler();

    public abstract java.lang.String getScoreboardEntryName();

    public abstract void broadcastHurtAnimation(java.util.Collection p0);
}
