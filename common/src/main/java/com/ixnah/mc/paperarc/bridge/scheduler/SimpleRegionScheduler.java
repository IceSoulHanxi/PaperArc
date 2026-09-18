package com.ixnah.mc.paperarc.bridge.scheduler;

import com.google.common.base.Preconditions;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

/**
 * Sync-fallback {@link RegionScheduler}（A5-3，pairing 基线 NO_IMPL）。
 *
 * <p>与 {@link SimpleGlobalRegionScheduler} 同样的取舍：PaperArc 没有 Folia 的区域线程，
 * 所有任务都走经典的主线程调度器，区域坐标（world/chunkX/chunkZ）只用于参数校验。
 * 非 Folia 的 Paper 自己也是这么退化的 —— 主线程即"所有区域的执行线程"。</p>
 */
public final class SimpleRegionScheduler implements RegionScheduler {

    @Override
    public void execute(Plugin plugin, World world, int chunkX, int chunkZ, Runnable run) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        Preconditions.checkArgument(world != null, "world cannot be null");
        Preconditions.checkArgument(run != null, "runnable cannot be null");
        SimpleScheduledTask.schedule(plugin, t -> run.run(), 0L, 0L, false);
    }

    @Override
    public ScheduledTask run(Plugin plugin, World world, int chunkX, int chunkZ,
                             Consumer<ScheduledTask> task) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        Preconditions.checkArgument(world != null, "world cannot be null");
        Preconditions.checkArgument(task != null, "task cannot be null");
        return SimpleScheduledTask.schedule(plugin, task, 0L, 0L, false);
    }

    @Override
    public ScheduledTask runDelayed(Plugin plugin, World world, int chunkX, int chunkZ,
                                    Consumer<ScheduledTask> task, long delayTicks) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        Preconditions.checkArgument(world != null, "world cannot be null");
        Preconditions.checkArgument(task != null, "task cannot be null");
        Preconditions.checkArgument(delayTicks >= 1, "delay must be at least 1 tick");
        return SimpleScheduledTask.schedule(plugin, task, delayTicks, 0L, false);
    }

    @Override
    public ScheduledTask runAtFixedRate(Plugin plugin, World world, int chunkX, int chunkZ,
                                        Consumer<ScheduledTask> task, long initialDelayTicks,
                                        long periodTicks) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        Preconditions.checkArgument(world != null, "world cannot be null");
        Preconditions.checkArgument(task != null, "task cannot be null");
        Preconditions.checkArgument(initialDelayTicks >= 1, "initial delay must be at least 1 tick");
        Preconditions.checkArgument(periodTicks >= 1, "period must be at least 1 tick");
        return SimpleScheduledTask.schedule(plugin, task, initialDelayTicks, periodTicks, false);
    }
}
