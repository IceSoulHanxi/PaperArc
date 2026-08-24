package com.ixnah.mc.paperarc.bridge;

import net.minecraft.world.level.portal.DimensionTransition;
import org.bukkit.Location;

/**
 * Mutable capture holder for the PlayerPostRespawnEvent pipeline. Lives
 * outside the mixin package because Sponge forbids direct references to
 * mixin-inner classes from merged code.
 */
public final class RespawnCapture {

    public boolean respawn;
    public boolean bedSpawn;
    public Location location;
    public DimensionTransition transition;
}
