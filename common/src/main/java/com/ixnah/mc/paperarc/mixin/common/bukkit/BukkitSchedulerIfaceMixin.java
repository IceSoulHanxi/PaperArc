package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.scheduler.BukkitScheduler} (generated).
 * Adds 1 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.scheduler.BukkitScheduler", remap = false)
public interface BukkitSchedulerIfaceMixin {

    @Unique
    public abstract java.util.concurrent.Executor getMainThreadExecutor(org.bukkit.plugin.Plugin p0);

}