package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.Location;
import org.bukkit.RegionAccessor;
import com.google.common.base.Preconditions;
import io.papermc.paper.block.fluid.FluidData;
import io.papermc.paper.math.Position;
import io.papermc.paper.world.MoonPhase;
import io.papermc.paper.world.flag.FeatureFlagSetHolder;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.BoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.RegionAccessor} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.RegionAccessor", remap = false)
public interface RegionAccessorIfaceMixin extends io.papermc.paper.world.flag.FeatureFlagSetHolder, org.bukkit.Keyed {

    @Unique
    public abstract org.bukkit.block.Biome getComputedBiome(int p0, int p1, int p2);

    @Unique
    public abstract io.papermc.paper.block.fluid.FluidData getFluidData(int p0, int p1, int p2);

    @Unique
    public abstract io.papermc.paper.world.MoonPhase getMoonPhase();

    @Unique
    public abstract org.bukkit.NamespacedKey getKey();

    @Unique
    public abstract boolean lineOfSightExists(org.bukkit.Location p0, org.bukkit.Location p1);

    @Unique
    public abstract boolean hasCollisionsIn(org.bukkit.util.BoundingBox p0);

    /**
     * 实现体是运行时 CraftBukkit 自带的（CraftRegionAccessor 上已有同签名方法），
     * 只有运行时**接口**少了这条声明，插件按接口调用才会 NoSuchMethodError。
     */
    @Unique
    public abstract org.bukkit.entity.Entity spawn(org.bukkit.Location p0, java.lang.Class p1, java.util.function.Consumer p2, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason p3);

    @Unique
    public default FluidData getFluidData(Position position) {
        RegionAccessor self = (RegionAccessor) this;
        return self.getFluidData(position.blockX(), position.blockY(), position.blockZ());
    }

    @Unique
    public default FluidData getFluidData(Location location) {
        RegionAccessor self = (RegionAccessor) this;
        return self.getFluidData(location.blockX(), location.blockY(), location.blockZ());
    }

    @Unique
    public default <T extends Entity> T spawn(Location location, Class<T> clazz, Consumer<? super T> function) throws IllegalArgumentException {
        RegionAccessor self = (RegionAccessor) this;
        return self.spawn(location, clazz, CreatureSpawnEvent.SpawnReason.CUSTOM, function);
    }

    @Unique
    public default <T extends Entity> T spawn(Location location, Class<T> clazz, CreatureSpawnEvent.SpawnReason reason) throws IllegalArgumentException {
        RegionAccessor self = (RegionAccessor) this;
        return self.spawn(location, clazz, reason, (java.util.function.Consumer<? super T>) null);
    }

    @Unique
    public default <T extends Entity> T spawn(Location location, Class<T> clazz, CreatureSpawnEvent.SpawnReason reason, Consumer<? super T> function) throws IllegalArgumentException {
        RegionAccessor self = (RegionAccessor) this;
        return self.spawn(location, clazz, function, reason);
    }

    @Unique
    public default Entity spawnEntity(Location loc, EntityType type, CreatureSpawnEvent.SpawnReason reason) {
        RegionAccessor self = (RegionAccessor) this;
        Preconditions.checkArgument(type.getEntityClass() != null, "%s is not a valid EntityType, must have an entity class", type);
        return self.spawn(loc, type.getEntityClass(), reason, (Consumer) null);
    }

    @Unique
    public default Entity spawnEntity(Location loc, EntityType type, CreatureSpawnEvent.SpawnReason reason, Consumer<? super Entity> function) {
        RegionAccessor self = (RegionAccessor) this;
        Preconditions.checkArgument(type.getEntityClass() != null, "%s is not a valid EntityType, must have an entity class", type);
        return self.spawn(loc, type.getEntityClass(), reason, function);
    }
}
