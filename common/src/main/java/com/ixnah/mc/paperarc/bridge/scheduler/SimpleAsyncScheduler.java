package com.ixnah.mc.paperarc.bridge.scheduler;

import com.google.common.base.Preconditions;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Sync-fallback {@link AsyncScheduler} (PaperArc batch blocked-1).
 *
 * <p><b>Not truly asynchronous in the Folia sense:</b> tasks are handed to the
 * classic Bukkit scheduler's async executor ({@code runTaskAsynchronously}),
 * so they run on Bukkit's shared async worker pool instead of dedicated Folia
 * async threads. Delay/rate parameters are converted from the requested
 * {@link TimeUnit} to ticks at 50 ms per tick.</p>
 */
public final class SimpleAsyncScheduler implements AsyncScheduler {

    /** Milliseconds per vanilla tick. */
    private static final long MS_PER_TICK = 50L;

    static long toTicks(long time) {
        return time <= 0 ? 0 : Math.max(1L, time / MS_PER_TICK);
    }

    @Override
    public ScheduledTask runNow(Plugin plugin, Consumer<ScheduledTask> task) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        Preconditions.checkArgument(task != null, "task cannot be null");
        return SimpleScheduledTask.schedule(plugin, task, 0L, 0L, true);
    }

    @Override
    public ScheduledTask runDelayed(Plugin plugin, Consumer<ScheduledTask> task,
                                    long delay, TimeUnit unit) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        Preconditions.checkArgument(task != null, "task cannot be null");
        Preconditions.checkArgument(unit != null, "unit cannot be null");
        return SimpleScheduledTask.schedule(plugin, task, toTicks(unit.toMillis(delay)), 0L, true);
    }

    @Override
    public ScheduledTask runAtFixedRate(Plugin plugin, Consumer<ScheduledTask> task,
                                        long initialDelay, long period, TimeUnit unit) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        Preconditions.checkArgument(task != null, "task cannot be null");
        Preconditions.checkArgument(unit != null, "unit cannot be null");
        Preconditions.checkArgument(period > 0, "period must be positive");
        return SimpleScheduledTask.schedule(plugin, task,
                toTicks(unit.toMillis(initialDelay)), toTicks(unit.toMillis(period)), true);
    }

    @Override
    public void cancelTasks(Plugin plugin) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        Bukkit.getServer().getScheduler().cancelTasks(plugin);
    }
}
