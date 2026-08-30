package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ambient.Bat;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftBat;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Bat target-location API.
 *
 * Paper publicizes the private NMS field {@code Bat.targetPosition} via an access
 * transformer; here it is widened via AT (f_27409_) and accessed directly —
 * no reflection.
 */
@Mixin(CraftBat.class)
public abstract class CraftBatApiMixin {

    @Shadow
    public abstract Bat getHandle();

    @Unique
    public Location getTargetLocation() {
        BlockPos pos = getHandle().targetPosition;
        if (pos == null) {
            return null;
        }
        return CraftLocation.toBukkit(pos, getHandle().level());
    }

    @Unique
    public void setTargetLocation(Location location) {
        BlockPos pos = location != null ? CraftLocation.toBlockPosition(location) : null;
        getHandle().targetPosition = pos;
    }
}
