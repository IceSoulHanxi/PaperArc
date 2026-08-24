package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.FishHook} (generated).
 * Adds 4 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.FishHook", remap = false)
public interface FishHookIfaceMixin {

    @Unique
    public abstract int getWaitTime();

    @Unique
    public abstract void setWaitTime(int p0);

    @Unique
    public abstract int getTimeUntilBite();

    @Unique
    public abstract void resetFishingState();
}
