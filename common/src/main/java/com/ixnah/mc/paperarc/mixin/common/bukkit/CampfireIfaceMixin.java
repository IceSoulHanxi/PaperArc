package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.Campfire} (generated).
 * Adds 5 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.block.Campfire", remap = false)
public interface CampfireIfaceMixin {

    @Unique
    public abstract void stopCooking();

    @Unique
    public abstract void startCooking();

    @Unique
    public abstract boolean stopCooking(int p0);

    @Unique
    public abstract boolean startCooking(int p0);

    @Unique
    public abstract boolean isCookingDisabled(int p0);
}
