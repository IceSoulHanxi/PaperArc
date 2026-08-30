package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.minecart.HopperMinecart} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.minecart.HopperMinecart", remap = false)
public interface HopperMinecartIfaceMixin {

    @Unique
    public abstract int getPickupCooldown();

    @Unique
    public abstract void setPickupCooldown(int p0);

}