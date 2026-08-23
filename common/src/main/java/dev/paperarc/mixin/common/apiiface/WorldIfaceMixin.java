package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.World} (generated).
 * Adds 31 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.World", remap = false)
public interface WorldIfaceMixin {

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
    public abstract java.util.concurrent.CompletableFuture getChunkAtAsync(int p0, int p1, boolean p2, boolean p3);

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
}
