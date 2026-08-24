package com.ixnah.mc.paperarc.bridge.scheduler;

import com.google.common.base.Preconditions;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

/**
 * Sync-fallback {@link GlobalRegionScheduler} (PaperArc batch blocked-1).
 *
 * <p><b>Runs everything on the main server thread:</b> PaperArc has no Folia
 * global region thread, so tasks are scheduled through the classic Bukkit
 * main-thread scheduler. Delays are already in ticks (the interface's native
 * unit), matching vanilla tick pacing.</p>
 */
public final class SimpleGlobalRegionScheduler implements GlobalRegionScheduler {

    @Override
    public void execute(Plugin plugin, Runnable runnable) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        Preconditions.checkArgument(runnable != null, "runnable cannot be null");
        // discard the wrapper handle: execute() is fire-and-forget
        SimpleScheduledTask.schedule(plugin, t -> runnable.run(), 0L, 0L, false);
    }

    @Override
    public ScheduledTask run(Plugin plugin, Consumer<ScheduledTask> task) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        Preconditions.checkArgument(task != null, "task cannot be null");
        return SimpleScheduledTask.schedule(plugin, task, 0L, 0L, false);
    }

    @Override
    public ScheduledTask runDelayed(Plugin plugin, Consumer<ScheduledTask> task, long delayTicks) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        Preconditions.checkArgument(task != null, "task cannot be null");
        Preconditions.checkArgument(delayTicks >= 1, "delay must be at least 1 tick");
        return SimpleScheduledTask.schedule(plugin, task, delayTicks, 0L, false);
    }

    @Override
    public ScheduledTask runAtFixedRate(Plugin plugin, Consumer<ScheduledTask> task,
                                        long initialDelayTicks, long periodTicks) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        Preconditions.checkArgument(task != null, "task cannot be null");
        Preconditions.checkArgument(initialDelayTicks >= 1, "initial delay must be at least 1 tick");
        Preconditions.checkArgument(periodTicks >= 1, "period must be at least 1 tick");
        return SimpleScheduledTask.schedule(plugin, task, initialDelayTicks, periodTicks, false);
    }

    @Override
    public void cancelTasks(Plugin plugin) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        Bukkit.getServer().getScheduler().cancelTasks(plugin);
    }
}
