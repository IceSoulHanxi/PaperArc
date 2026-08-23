package dev.paperarc.bridge.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Minimal {@link ScheduledTask} implementation backed by the classic Bukkit
 * scheduler (sync-fallback).
 *
 * <p>PaperArc keeps a vanilla single-thread server model: executions are routed
 * to the main server thread via {@code Bukkit.getServer().getScheduler()}
 * (or Bukkit's shared async pool when scheduled through the {@code
 * SimpleAsyncScheduler} fallback). There are no Folia region threads and no
 * real futures — this class is <b>not</b> truly asynchronous. Cancel/state
 * semantics are intentionally simplified: state transitions are guarded by an
 * atomic counter so a task runs exactly once per scheduled tick.</p>
 */
public final class SimpleScheduledTask implements ScheduledTask {

    /** Bit flags mirroring ExecutionState without extra allocation. */
    private static final int ST_IDLE = 0;
    private static final int ST_RUNNING = 1;
    private static final int ST_FINISHED = 2;
    private static final int ST_CANCELLED = 3;
    private static final int ST_CANCELLED_RUNNING = 4;

    private final Plugin plugin;
    private final Consumer<ScheduledTask> runner;
    private final boolean repeating;

    private volatile BukkitTask handle;
    private final AtomicInteger state = new AtomicInteger(ST_IDLE);

    private SimpleScheduledTask(Plugin plugin, Consumer<ScheduledTask> runner, boolean repeating) {
        this.plugin = plugin;
        this.runner = runner;
        this.repeating = repeating;
    }

    /**
     * Schedules {@code runner} through the Bukkit scheduler.
     *
     * @param delayTicks  initial delay in ticks (clamped to &ge; 0)
     * @param periodTicks period in ticks; &gt; 0 makes the task repeating
     * @param async       route to Bukkit's async executor instead of main thread
     */
    public static SimpleScheduledTask schedule(Plugin plugin, Consumer<ScheduledTask> runner,
                                               long delayTicks, long periodTicks, boolean async) {
        boolean repeating = periodTicks > 0;
        SimpleScheduledTask task = new SimpleScheduledTask(plugin, runner, repeating);
        Runnable body = task::runBody;
        BukkitScheduler bs = Bukkit.getServer().getScheduler();
        long delay = Math.max(0L, delayTicks);
        BukkitTask h;
        if (repeating) {
            long period = Math.max(1L, periodTicks);
            h = async ? bs.runTaskTimerAsynchronously(plugin, body, delay, period)
                      : bs.runTaskTimer(plugin, body, delay, period);
        } else if (delay > 0) {
            h = async ? bs.runTaskLaterAsynchronously(plugin, body, delay)
                      : bs.runTaskLater(plugin, body, delay);
        } else {
            h = async ? bs.runTaskAsynchronously(plugin, body)
                      : bs.runTask(plugin, body);
        }
        task.handle = h;
        return task;
    }

    private void runBody() {
        if (!state.compareAndSet(ST_IDLE, ST_RUNNING)) {
            return; // cancelled before run, or already executing (skipped repeat overlap)
        }
        try {
            this.runner.accept(this);
        } finally {
            if (!repeating) {
                state.compareAndSet(ST_RUNNING, ST_FINISHED);
            } else if (!state.compareAndSet(ST_RUNNING, ST_IDLE)) {
                // cancel() flipped it to CANCELLED_RUNNING while we ran; stop future repeats
                cancelHandle();
            }
        }
    }

    private void cancelHandle() {
        BukkitTask h = this.handle;
        if (h != null) {
            h.cancel();
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
                        return CancelledState.RUNNING; // single-shot will finish anyway
                    }
                    if (state.compareAndSet(cur, ST_CANCELLED_RUNNING)) {
                        cancelHandle();
                        return CancelledState.NEXT_RUNS_CANCELLED;
                    }
                }
                default -> { // IDLE
                    if (state.compareAndSet(cur, ST_CANCELLED)) {
                        cancelHandle();
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
