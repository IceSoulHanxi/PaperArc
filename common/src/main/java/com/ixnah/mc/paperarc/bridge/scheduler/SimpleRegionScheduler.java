package com.ixnah.mc.paperarc.bridge.scheduler;

import com.google.common.base.Preconditions;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

/**
 * Sync-fallback {@link RegionScheduler}（与 {@link SimpleGlobalRegionScheduler} 同构）。
 *
 * <p>PaperArc 没有 Folia 的区域线程，所有"某个区块所属区域"的任务一律走经典的 Bukkit
 * 主线程调度器 —— 这与非 Folia 的 Paper 行为一致。world/chunkX/chunkZ 只用来做参数校验。</p>
 */
public final class SimpleRegionScheduler implements RegionScheduler {

    @Override
    public void execute(Plugin plugin, World world, int chunkX, int chunkZ, Runnable run) {
        paperarc$check(plugin, world, run);
        SimpleScheduledTask.schedule(plugin, t -> run.run(), 0L, 0L, false);
    }

    @Override
    public ScheduledTask run(Plugin plugin, World world, int chunkX, int chunkZ, Consumer<ScheduledTask> task) {
        paperarc$check(plugin, world, task);
        return SimpleScheduledTask.schedule(plugin, task, 0L, 0L, false);
    }

    @Override
    public ScheduledTask runDelayed(Plugin plugin, World world, int chunkX, int chunkZ,
                                    Consumer<ScheduledTask> task, long delayTicks) {
        paperarc$check(plugin, world, task);
        Preconditions.checkArgument(delayTicks >= 1, "delay must be at least 1 tick");
        return SimpleScheduledTask.schedule(plugin, task, delayTicks, 0L, false);
    }

    @Override
    public ScheduledTask runAtFixedRate(Plugin plugin, World world, int chunkX, int chunkZ,
                                        Consumer<ScheduledTask> task, long initialDelayTicks, long periodTicks) {
        paperarc$check(plugin, world, task);
        Preconditions.checkArgument(initialDelayTicks >= 1, "initial delay must be at least 1 tick");
        Preconditions.checkArgument(periodTicks >= 1, "period must be at least 1 tick");
        return SimpleScheduledTask.schedule(plugin, task, initialDelayTicks, periodTicks, false);
    }

    private static void paperarc$check(Plugin plugin, World world, Object task) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        Preconditions.checkArgument(world != null, "world cannot be null");
        Preconditions.checkArgument(task != null, "task cannot be null");
    }
}
