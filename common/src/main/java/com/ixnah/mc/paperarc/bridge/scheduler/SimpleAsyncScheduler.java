package com.ixnah.mc.paperarc.bridge.scheduler;

import com.google.common.base.Preconditions;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.bukkit.plugin.Plugin;

/**
 * {@link AsyncScheduler} 实现（PaperArc；Phase A2-2 由 tick 换算改为墙钟计时）。
 *
 * <p>任务跑在 {@link AsyncScheduledTask} 的专用守护线程池上，delay/period 按调用方
 * 给的 {@link TimeUnit} 直接计时 —— 与 Paper/Folia 的 {@code AsyncScheduler} 语义一致。
 * 之前是换算成 tick 交给 Bukkit 的 {@code runTaskTimerAsynchronously}：精度只有 50ms，
 * 且随主线程 tick 循环漂移（服务器卡顿时异步任务跟着延后）。
 *
 * <p>仍与 Folia 不同的地方写在 {@link AsyncScheduledTask} 的类注释里。
 */
public final class SimpleAsyncScheduler implements AsyncScheduler {

    @Override
    public ScheduledTask runNow(Plugin plugin, Consumer<ScheduledTask> task) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        Preconditions.checkArgument(task != null, "task cannot be null");
        return AsyncScheduledTask.schedule(plugin, task, 0L, 0L);
    }

    @Override
    public ScheduledTask runDelayed(Plugin plugin, Consumer<ScheduledTask> task,
                                    long delay, TimeUnit unit) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        Preconditions.checkArgument(task != null, "task cannot be null");
        Preconditions.checkArgument(unit != null, "unit cannot be null");
        return AsyncScheduledTask.schedule(plugin, task, unit.toMillis(delay), 0L);
    }

    @Override
    public ScheduledTask runAtFixedRate(Plugin plugin, Consumer<ScheduledTask> task,
                                        long initialDelay, long period, TimeUnit unit) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        Preconditions.checkArgument(task != null, "task cannot be null");
        Preconditions.checkArgument(unit != null, "unit cannot be null");
        Preconditions.checkArgument(period > 0, "period must be positive");
        return AsyncScheduledTask.schedule(plugin, task,
                unit.toMillis(initialDelay), Math.max(1L, unit.toMillis(period)));
    }

    @Override
    public void cancelTasks(Plugin plugin) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        AsyncScheduledTask.cancelAll(plugin);
    }
}
