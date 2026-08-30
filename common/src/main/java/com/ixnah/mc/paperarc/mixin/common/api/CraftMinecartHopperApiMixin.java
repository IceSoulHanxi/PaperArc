package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v1_20_R1.entity.CraftMinecartHopper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's missing-entity-behaviour API {@code HopperMinecart} cooldown methods
 * to {@link CraftMinecartHopper}.
 *
 * <p>Paper explicitly throws {@link UnsupportedOperationException} because hopper
 * minecarts don't have pickup cooldowns (unlike the hopper block); the two methods
 * exist only for interface completeness. Mirrored verbatim.</p>
 */
@Mixin(CraftMinecartHopper.class)
public abstract class CraftMinecartHopperApiMixin {

    @Unique
    public int getPickupCooldown() {
        throw new UnsupportedOperationException("Hopper minecarts don't have cooldowns");
    }

    @Unique
    public void setPickupCooldown(int cooldown) {
        throw new UnsupportedOperationException("Hopper minecarts don't have cooldowns");
    }
}
