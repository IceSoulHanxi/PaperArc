package com.ixnah.mc.paperarc.bridge.api;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.destroystokyo.paper.entity.ai.MobGoals;
import org.bukkit.craftbukkit.v.entity.CraftMob;
import org.bukkit.entity.Mob;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;

/**
 * Minimal {@link MobGoals} implementation backed by the NMS goal selectors
 * (sync-fallback quality, PaperArc batch blocked-1).
 *
 * <p><b>Simplifications vs Paper's CraftMobGoals:</b></p>
 * <ul>
 *   <li>Only goals added through this manager are visible; vanilla-spawned
 *       NMS goals are not wrapped/registered (Paper uses a VanillaGoal
 *       registry we do not have).</li>
 *   <li>All goals are added to {@code Mob.goalSelector} regardless of their
 *       {@link GoalType}; target-selector routing is not implemented.</li>
 *   <li>Running-state detection relies on the adapter's own start/stop
 *       bookkeeping.</li>
 * </ul>
 */
public final class SimpleMobGoals implements MobGoals {

    /** One API-added goal and its NMS adapter. */
    private record TrackedGoal<T extends Mob>(int priority, Goal<T> goal, AdapterGoal<T> adapter) {
    }

    /** Bridges a Paper {@link Goal} into a vanilla NMS goal. */
    private static final class AdapterGoal<T extends Mob> extends net.minecraft.world.entity.ai.goal.Goal {
        private final Goal<T> delegate;
        private boolean running;

        AdapterGoal(Goal<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean canUse() {
            return this.delegate.shouldActivate();
        }

        @Override
        public boolean canContinueToUse() {
            return this.delegate.shouldStayActive();
        }

        @Override
        public void start() {
            this.running = true;
            this.delegate.start();
        }

        @Override
        public void stop() {
            try {
                this.delegate.stop();
            } finally {
                this.running = false;
            }
        }

        @Override
        public void tick() {
            this.delegate.tick();
        }

        boolean isAdapterRunning() {
            return this.running;
        }
    }

    private final Map<Mob, List<TrackedGoal<?>>> tracked = new WeakHashMap<>();

    /**
     * {@code Mob.goalSelector} is protected on this compile/runtime classpath;
     * the Access Widener cannot cover it here (batch constraint forbids touching
     * other files), so reach it through a lazily-cached reflective getter.
     */
    private static volatile java.lang.reflect.Field PAPERARC_GOAL_SELECTOR_FIELD;

    private static net.minecraft.world.entity.ai.goal.GoalSelector goalSelectorOf(
            net.minecraft.world.entity.Mob mob) {
        try {
            java.lang.reflect.Field field = PAPERARC_GOAL_SELECTOR_FIELD;
            if (field == null) {
                field = net.minecraft.world.entity.Mob.class.getDeclaredField("goalSelector");
                field.setAccessible(true);
                PAPERARC_GOAL_SELECTOR_FIELD = field;
            }
            return (net.minecraft.world.entity.ai.goal.GoalSelector) field.get(mob);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("PaperArc: cannot access Mob.goalSelector", e);
        }
    }

    private synchronized List<TrackedGoal<?>> snapshot(Mob mob) {
        List<TrackedGoal<?>> list = this.tracked.get(mob);
        return list == null ? Collections.emptyList() : new ArrayList<>(list);
    }

    private net.minecraft.world.entity.Mob nms(Mob mob) {
        if (!(mob instanceof CraftMob craftMob)) {
            throw new IllegalArgumentException("Mob is not a CraftBukkit mob: " + mob);
        }
        return craftMob.getHandle();
    }

    @Override
    public <T extends Mob> void addGoal(T mob, int priority, Goal<T> goal) {
        java.util.Objects.requireNonNull(mob, "mob cannot be null");
        java.util.Objects.requireNonNull(goal, "goal cannot be null");
        net.minecraft.world.entity.Mob handle = nms(mob);
        AdapterGoal<T> adapter = new AdapterGoal<>(goal);
        synchronized (this) {
            List<TrackedGoal<?>> list = this.tracked.computeIfAbsent(mob, k -> new ArrayList<>());
            for (TrackedGoal<?> existing : list) {
                if (existing.goal() == goal) {
                    return; // already tracked
                }
            }
            list.add(new TrackedGoal<>(priority, goal, adapter));
        }
        goalSelectorOf(handle).addGoal(priority, adapter);
    }

    @Override
    public <T extends Mob> void removeGoal(T mob, Goal<T> goal) {
        removeTracked(mob, e -> e.goal() == goal);
    }

    @Override
    public <T extends Mob> void removeAllGoals(T mob) {
        removeTracked(mob, e -> true);
    }

    @Override
    public <T extends Mob> void removeAllGoals(T mob, GoalType type) {
        java.util.Objects.requireNonNull(type, "type cannot be null");
        removeTracked(mob, e -> e.goal().getTypes().contains(type));
    }

    @Override
    public <T extends Mob> void removeGoal(T mob, GoalKey<T> key) {
        java.util.Objects.requireNonNull(key, "key cannot be null");
        removeTracked(mob, e -> key.equals(e.goal().getKey()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void removeTracked(Mob mob, Predicate<TrackedGoal<?>> filter) {
        java.util.Objects.requireNonNull(mob, "mob cannot be null");
        net.minecraft.world.entity.Mob handle = null;
        List<TrackedGoal<?>> removed = new ArrayList<>();
        synchronized (this) {
            List<TrackedGoal<?>> list = this.tracked.get(mob);
            if (list != null) {
                for (TrackedGoal<?> e : new ArrayList<>(list)) {
                    if (filter.test((TrackedGoal) e)) {
                        removed.add(e);
                        list.remove(e);
                    }
                }
                if (list.isEmpty()) {
                    this.tracked.remove(mob);
                }
            }
        }
        if (!removed.isEmpty()) {
            handle = nms(mob);
            for (TrackedGoal<?> e : removed) {
                goalSelectorOf(handle).removeGoal(e.adapter());
            }
        }
    }

    @Override
    public <T extends Mob> boolean hasGoal(T mob, GoalKey<T> key) {
        return !goalsMatching(mob, e -> key.equals(e.goal().getKey())).isEmpty();
    }

    @Override
    public <T extends Mob> Goal<T> getGoal(T mob, GoalKey<T> key) {
        Collection<Goal<T>> found = goalsMatching(mob, e -> key.equals(e.goal().getKey()));
        return found.isEmpty() ? null : found.iterator().next();
    }

    @Override
    public <T extends Mob> Collection<Goal<T>> getGoals(T mob, GoalKey<T> key) {
        return goalsMatching(mob, e -> key.equals(e.goal().getKey()));
    }

    @Override
    public <T extends Mob> Collection<Goal<T>> getAllGoals(T mob) {
        return goalsMatching(mob, e -> true);
    }

    @Override
    public <T extends Mob> Collection<Goal<T>> getAllGoals(T mob, GoalType type) {
        java.util.Objects.requireNonNull(type, "type cannot be null");
        return goalsMatching(mob, e -> e.goal().getTypes().contains(type));
    }

    @Override
    public <T extends Mob> Collection<Goal<T>> getAllGoalsWithout(T mob, GoalType type) {
        java.util.Objects.requireNonNull(type, "type cannot be null");
        return goalsMatching(mob, e -> !e.goal().getTypes().contains(type));
    }

    @Override
    public <T extends Mob> Collection<Goal<T>> getRunningGoals(T mob) {
        return goalsMatching(mob, e -> e.adapter().isAdapterRunning());
    }

    @Override
    public <T extends Mob> Collection<Goal<T>> getRunningGoals(T mob, GoalType type) {
        java.util.Objects.requireNonNull(type, "type cannot be null");
        return goalsMatching(mob, e -> e.adapter().isAdapterRunning()
                && e.goal().getTypes().contains(type));
    }

    @Override
    public <T extends Mob> Collection<Goal<T>> getRunningGoalsWithout(T mob, GoalType type) {
        java.util.Objects.requireNonNull(type, "type cannot be null");
        return goalsMatching(mob, e -> e.adapter().isAdapterRunning()
                && !e.goal().getTypes().contains(type));
    }

    @SuppressWarnings("unchecked")
    private <T extends Mob> Collection<Goal<T>> goalsMatching(Mob mob,
                                                              Predicate<TrackedGoal<?>> filter) {
        List<Goal<T>> result = new ArrayList<>();
        for (TrackedGoal<?> e : snapshot(mob)) {
            if (filter.test(e)) {
                result.add((Goal<T>) e.goal());
            }
        }
        return result;
    }
}
