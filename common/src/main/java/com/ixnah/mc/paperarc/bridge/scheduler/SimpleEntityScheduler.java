package com.ixnah.mc.paperarc.bridge.scheduler;

import com.google.common.base.Preconditions;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

/**
 * Sync-fallback {@link EntityScheduler} wrapping a Bukkit entity (PaperArc
 * batch blocked-1).
 *
 * <p><b>Not Folia entity scheduling:</b> tasks run on the main server thread
 * via the classic Bukkit scheduler. The retired callback fires instead of the
 * task body when the wrapped entity is no longer {@link Entity#isValid()}
 * (removed/unloaded) at execution time — a simplified stand-in for Folia's
 * entity-retirement semantics. Tasks already queued are not proactively
 * cancelled when the entity dies mid-delay; they retire on their next run.</p>
 */
public final class SimpleEntityScheduler implements EntityScheduler {

    private final Entity entity;

    public SimpleEntityScheduler(Entity entity) {
        this.entity = java.util.Objects.requireNonNull(entity, "entity cannot be null");
    }

    private boolean retired() {
        return !this.entity.isValid();
    }

    private Consumer<ScheduledTask> guarded(Runnable run, Runnable retiredCallback) {
        return task -> {
            if (retired()) {
                if (retiredCallback != null) {
                    retiredCallback.run();
                }
                task.cancel();
                return;
            }
            run.run();
        };
    }

    private Consumer<ScheduledTask> guardedConsumer(Consumer<ScheduledTask> task, Runnable retiredCallback) {
        return scheduled -> {
            if (retired()) {
                if (retiredCallback != null) {
                    retiredCallback.run();
                }
                scheduled.cancel();
                return;
            }
            task.accept(scheduled);
        };
    }

    @Override
    public boolean execute(Plugin plugin, Runnable run, Runnable retired, long delayTicks) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        Preconditions.checkArgument(run != null, "run cannot be null");
        if (retired()) {
            if (retired != null) {
                retired.run();
            }
            return false;
        }
        SimpleScheduledTask.schedule(plugin, guarded(run, retired),
                Math.max(1L, delayTicks), 0L, false);
        return true;
    }

    @Override
    public ScheduledTask run(Plugin plugin, Consumer<ScheduledTask> task, Runnable retired) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        Preconditions.checkArgument(task != null, "task cannot be null");
        return runDelayed(plugin, task, retired, 1L);
    }

    @Override
    public ScheduledTask runDelayed(Plugin plugin, Consumer<ScheduledTask> task,
                                    Runnable retired, long delayTicks) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        Preconditions.checkArgument(task != null, "task cannot be null");
        Preconditions.checkArgument(delayTicks >= 1, "delay must be at least 1 tick");
        return SimpleScheduledTask.schedule(plugin, guardedConsumer(task, retired),
                delayTicks, 0L, false);
    }

    @Override
    public ScheduledTask runAtFixedRate(Plugin plugin, Consumer<ScheduledTask> task,
                                        Runnable retired, long initialDelayTicks, long periodTicks) {
        Preconditions.checkArgument(plugin != null, "plugin cannot be null");
        Preconditions.checkArgument(task != null, "task cannot be null");
        Preconditions.checkArgument(initialDelayTicks >= 1, "initial delay must be at least 1 tick");
        Preconditions.checkArgument(periodTicks >= 1, "period must be at least 1 tick");
        return SimpleScheduledTask.schedule(plugin, guardedConsumer(task, retired),
                initialDelayTicks, periodTicks, false);
    }
}
