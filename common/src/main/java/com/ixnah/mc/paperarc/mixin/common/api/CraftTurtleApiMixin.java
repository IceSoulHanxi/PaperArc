package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Turtle;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftTurtle;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Turtle-API to {@link CraftTurtle}.
 *
 * <p>{@code Turtle.getHomePos()}, {@code Turtle.isGoingHome()} and
 * {@code Turtle.setHasEgg(boolean)} are package-private in vanilla 1.20.1,
 * widened via AT (m_30205_ / m_30211_ / m_30234_) and called directly;
 * the remaining calls use the public NMS accessors.</p>
 */
@Mixin(CraftTurtle.class)
public abstract class CraftTurtleApiMixin {

    @Shadow
    public abstract Turtle getHandle();

    @Unique
    public Location getHome() {
        BlockPos pos = getHandle().getHomePos();
        return CraftLocation.toBukkit(pos, getHandle().level());
    }

    @Unique
    public void setHome(Location location) {
        getHandle().setHomePos(CraftLocation.toBlockPosition(location));
    }

    @Unique
    public boolean isGoingHome() {
        return getHandle().isGoingHome();
    }

    @Unique
    public boolean isDigging() {
        return getHandle().isLayingEgg();
    }

    @Unique
    public void setHasEgg(boolean hasEgg) {
        getHandle().setHasEgg(hasEgg);
    }
}
