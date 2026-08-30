package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.minecart.PoweredMinecart} (generated).
 * Adds 4 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.minecart.PoweredMinecart", remap = false)
public interface PoweredMinecartIfaceMixin {

    @Unique
    public abstract double getPushX();

    @Unique
    public abstract double getPushZ();

    @Unique
    public abstract void setPushX(double p0);

    @Unique
    public abstract void setPushZ(double p0);

}