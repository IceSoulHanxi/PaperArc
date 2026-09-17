package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Turtle;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.entity.CraftTurtle;
import org.bukkit.craftbukkit.v.util.CraftLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Turtle-API to {@link CraftTurtle}.
 *
 * <p>{@code Turtle.getHomePos()}, {@code Turtle.isGoingHome()} and
 * {@code Turtle.setHasEgg(boolean)} are package-private in vanilla 1.21.1 and
 * opened by {@code paperarc.accesswidener}; the remaining calls use the public
 * NMS accessors.</p>
 */
@Mixin(CraftTurtle.class)
public abstract class CraftTurtleApiMixin {

    @Shadow
    public abstract Turtle getHandle();

    @Unique
    public Location getHome() {
        return CraftLocation.toBukkit(getHandle().getHomePos(), getHandle().level());
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
