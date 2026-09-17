package com.ixnah.mc.paperarc.bridge.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.bukkit.plugin.Plugin;

/**
 * {@link ScheduledTask} backed by a wall-clock {@link ScheduledExecutorService}
 * (PaperArc Phase A2-2).
 *
 * <p>取代原先把 {@code AsyncScheduler} 的 delay/period 换算成 tick 再交给 Bukkit
 * {@code runTaskTimerAsynchronously} 的做法：Bukkit 的 tick 定时精度只有 50ms，
 * 且随主线程 tick 循环漂移（服务器卡顿时异步任务也跟着延后），与 Paper/Folia
 * {@code AsyncScheduler} 承诺的墙钟语义不符。现在直接按 {@link TimeUnit} 计时。
 *
 * <p>与 Paper/Folia 的差异：
 * <ul>
 *   <li>线程池固定 {@value #POOL_SIZE} 条守护线程；同时运行的长任务超过这个数量时
 *       后续任务会排队（Folia 的池大小同样有限，性质相同）。</li>
 *   <li>Bukkit 调度器会在插件 disable 时自动取消它的任务，这个池不归 Bukkit 管，
 *       因此每次执行前检查 {@code plugin.isEnabled()}，插件已卸载就自行取消。</li>
 * </ul>
 */
public final class AsyncScheduledTask implements ScheduledTask {

    private static final int POOL_SIZE = 4;

    private static final ScheduledExecutorService EXECUTOR =
            Executors.newScheduledThreadPool(POOL_SIZE, new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger();

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "PaperArc Async Scheduler #" + counter.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                }
            });

    /** 每插件任务登记表，支撑 {@code AsyncScheduler#cancelTasks(Plugin)}。 */
    private static final ConcurrentHashMap<Plugin, Set<AsyncScheduledTask>> BY_PLUGIN = new ConcurrentHashMap<>();

    private static final int ST_IDLE = 0;
    private static final int ST_RUNNING = 1;
    private static final int ST_FINISHED = 2;
    private static final int ST_CANCELLED = 3;
    private static final int ST_CANCELLED_RUNNING = 4;

    private final Plugin plugin;
    private final Consumer<ScheduledTask> runner;
    private final boolean repeating;
    private final AtomicInteger state = new AtomicInteger(ST_IDLE);

    private volatile ScheduledFuture<?> handle;

    private AsyncScheduledTask(Plugin plugin, Consumer<ScheduledTask> runner, boolean repeating) {
        this.plugin = plugin;
        this.runner = runner;
        this.repeating = repeating;
    }

    /**
     * @param delayMs  初始延迟（毫秒，负数按 0）
     * @param periodMs 周期（毫秒）；&gt; 0 表示重复任务
     */
    public static AsyncScheduledTask schedule(Plugin plugin, Consumer<ScheduledTask> runner,
                                              long delayMs, long periodMs) {
        boolean repeating = periodMs > 0;
        AsyncScheduledTask task = new AsyncScheduledTask(plugin, runner, repeating);
        BY_PLUGIN.computeIfAbsent(plugin, p -> ConcurrentHashMap.newKeySet()).add(task);
        long delay = Math.max(0L, delayMs);
        task.handle = repeating
                ? EXECUTOR.scheduleAtFixedRate(task::runBody, delay, periodMs, TimeUnit.MILLISECONDS)
                : EXECUTOR.schedule(task::runBody, delay, TimeUnit.MILLISECONDS);
        // schedule 与 cancel 可能竞争：登记后若已被取消，补一次 cancel
        if (task.isCancelled()) {
            task.cancelHandle();
        }
        return task;
    }

    public static void cancelAll(Plugin plugin) {
        Set<AsyncScheduledTask> tasks = BY_PLUGIN.remove(plugin);
        if (tasks != null) {
            for (AsyncScheduledTask task : tasks) {
                task.cancel();
            }
        }
    }

    private void unregister() {
        Set<AsyncScheduledTask> tasks = BY_PLUGIN.get(this.plugin);
        if (tasks != null) {
            tasks.remove(this);
        }
    }

    private void runBody() {
        if (!this.plugin.isEnabled()) {
            cancel();
            return;
        }
        if (!state.compareAndSet(ST_IDLE, ST_RUNNING)) {
            return; // 已取消，或上一轮还没跑完（重复任务不重入）
        }
        try {
            this.runner.accept(this);
        } catch (Throwable t) {
            this.plugin.getLogger().warning("Async scheduled task threw: " + t);
        } finally {
            if (!repeating) {
                state.compareAndSet(ST_RUNNING, ST_FINISHED);
                unregister();
            } else if (!state.compareAndSet(ST_RUNNING, ST_IDLE)) {
                cancelHandle();
                unregister();
            }
        }
    }

    private void cancelHandle() {
        ScheduledFuture<?> h = this.handle;
        if (h != null) {
            h.cancel(false);
        }
    }

    @Override
    public Plugin getOwningPlugin() {
        return this.plugin;
    }

    @Override
    public boolean isRepeatingTask() {
        return this.repeating;
    }

    @Override
    public CancelledState cancel() {
        int cur = state.get();
        while (true) {
            switch (cur) {
                case ST_CANCELLED, ST_CANCELLED_RUNNING -> {
                    return CancelledState.CANCELLED_ALREADY;
                }
                case ST_FINISHED -> {
                    return CancelledState.ALREADY_EXECUTED;
                }
                case ST_RUNNING -> {
                    if (!repeating) {
                        return CancelledState.RUNNING;
                    }
                    if (state.compareAndSet(cur, ST_CANCELLED_RUNNING)) {
                        cancelHandle();
                        return CancelledState.NEXT_RUNS_CANCELLED;
                    }
                }
                default -> {
                    if (state.compareAndSet(cur, ST_CANCELLED)) {
                        cancelHandle();
                        unregister();
                        return CancelledState.CANCELLED_BY_CALLER;
                    }
                }
            }
            cur = state.get();
        }
    }

    @Override
    public ExecutionState getExecutionState() {
        return switch (state.get()) {
            case ST_RUNNING -> ExecutionState.RUNNING;
            case ST_FINISHED -> ExecutionState.FINISHED;
            case ST_CANCELLED -> ExecutionState.CANCELLED;
            case ST_CANCELLED_RUNNING -> ExecutionState.CANCELLED_RUNNING;
            default -> ExecutionState.IDLE;
        };
    }

    @Override
    public boolean isCancelled() {
        int s = state.get();
        return s == ST_CANCELLED || s == ST_CANCELLED_RUNNING;
    }
}
