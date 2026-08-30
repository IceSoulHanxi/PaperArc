package com.ixnah.mc.paperarc.mixin.common.api;

import java.util.concurrent.Executor;

import com.google.common.base.Preconditions;

import org.bukkit.craftbukkit.v1_20_R1.scheduler.CraftScheduler;
import org.bukkit.plugin.Plugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's {@code Add-getMainThreadExecutor-to-BukkitScheduler.patch} to
 * {@link CraftScheduler}: {@code getMainThreadExecutor(Plugin)} returns an
 * {@link Executor} that schedules tasks on the main thread via the classic
 * Bukkit scheduler (mirrors Paper's lambda implementation).
 */
@Mixin(CraftScheduler.class)
public abstract class CraftSchedulerApiMixin {

    @Shadow
    public abstract org.bukkit.scheduler.BukkitTask runTask(Plugin plugin, Runnable task);

    @Unique
    public Executor getMainThreadExecutor(Plugin plugin) {
        Preconditions.checkArgument(plugin != null, "Plugin cannot be null");
        return command -> {
            Preconditions.checkArgument(command != null, "Command cannot be null");
            this.runTask(plugin, command);
        };
    }
}
