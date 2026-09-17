package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.craftbukkit.v.scoreboard.CraftScoreboard;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Ports Paper additions missing from Arclight CraftBukkit {@link CraftScoreboard}:
 * <ul>
 *   <li>Paper "Improve-scoreboard-entries": {@code getEntityTeam(Entity)},
 *       {@code getScoresFor(Entity)} and {@code resetScoresFor(Entity)}. These are pure
 *       entry-string conveniences, so they delegate to the existing String-based
 *       {@code getEntryTeam}/{@code getScores}/{@code resetScores} keyed by the entity's
 *       scoreboard name ({@link Entity#getName()}, identical to NMS
 *       {@code Entity#getScoreBoardName()}).</li>
 *   <li>Paper "Adventure": {@code registerNewObjective} overloads taking a
 *       {@link Component} display name. Components are serialized to legacy Strings
 *       (same strategy as CraftMenuTypeApiMixin / CraftSignSideApiMixin, since this
 *       codebase keeps the String-based display-name path); the 3-arg forms default to
 *       {@link RenderType#INTEGER} like Paper.</li>
 * </ul>
 *
 * The existing String-based {@code registerNewObjective} overloads return the
 * package-private {@code CraftObjective}, which is not referenceable from this package,
 * so they are invoked reflectively (cached {@link Method}s) and surfaced as the public
 * {@link Objective} interface type declared by paper-api.
 */
@Mixin(CraftScoreboard.class)
public abstract class CraftScoreboardApiMixin {

    @Unique
    private static volatile Method PAPERARC$REGISTER_STRING;

    @Unique
    private static volatile Method PAPERARC$REGISTER_CRITERIA;

    @Shadow
    public abstract Team getEntryTeam(String entry);

    @Shadow
    public abstract Set<Score> getScores(String entry);

    @Shadow
    public abstract void resetScores(String entry);

    // ---- Improve-scoreboard-entries ----

    @Unique
    public Team getEntityTeam(Entity entity) {
        Preconditions.checkArgument(entity != null, "Entity cannot be null");
        return this.getEntryTeam(paperarc$scoreboardName(entity));
    }

    @Unique
    public Set<Score> getScoresFor(Entity entity) {
        Preconditions.checkArgument(entity != null, "Entity cannot be null");
        return this.getScores(paperarc$scoreboardName(entity));
    }

    @Unique
    public void resetScoresFor(Entity entity) {
        Preconditions.checkArgument(entity != null, "Entity cannot be null");
        this.resetScores(paperarc$scoreboardName(entity));
    }

    // ---- Adventure registerNewObjective ----

    @Unique
    public Objective registerNewObjective(String name, String criteria, Component displayName) {
        return this.registerNewObjective(name, criteria, displayName, RenderType.INTEGER);
    }

    @Unique
    public Objective registerNewObjective(String name, String criteria, Component displayName, RenderType renderType) {
        Preconditions.checkArgument(displayName != null, "displayName cannot be null");
        String legacy = LegacyComponentSerializer.legacySection().serialize(displayName);
        try {
            return (Objective) this.paperarc$registerString().invoke(this, name, criteria, legacy, renderType);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS registerNewObjective not invocable on CraftScoreboard", e);
        }
    }

    @Unique
    public Objective registerNewObjective(String name, Criteria criteria, Component displayName) {
        return this.registerNewObjective(name, criteria, displayName, RenderType.INTEGER);
    }

    @Unique
    public Objective registerNewObjective(String name, Criteria criteria, Component displayName, RenderType renderType) {
        Preconditions.checkArgument(displayName != null, "displayName cannot be null");
        String legacy = LegacyComponentSerializer.legacySection().serialize(displayName);
        try {
            return (Objective) this.paperarc$registerCriteria().invoke(this, name, criteria, legacy, renderType);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS registerNewObjective not invocable on CraftScoreboard", e);
        }
    }

    // ---- helpers ----

    // 反射目标是 CraftBukkit 侧成员（不参与 srg 重映射），按字面名查找在运行时正确；
    // NMS 成员必须改走 AT/@Accessor —— 见 docs/execution-plan-2026-09-16.md A2。

    @Unique
    private static Method paperarc$registerString() throws ReflectiveOperationException {
        Method m = PAPERARC$REGISTER_STRING;
        if (m == null) {
            m = CraftScoreboard.class.getDeclaredMethod("registerNewObjective",
                    String.class, String.class, String.class, RenderType.class);
            m.setAccessible(true);
            PAPERARC$REGISTER_STRING = m;
        }
        return m;
    }

    @Unique
    private static Method paperarc$registerCriteria() throws ReflectiveOperationException {
        Method m = PAPERARC$REGISTER_CRITERIA;
        if (m == null) {
            m = CraftScoreboard.class.getDeclaredMethod("registerNewObjective",
                    String.class, Criteria.class, String.class, RenderType.class);
            m.setAccessible(true);
            PAPERARC$REGISTER_CRITERIA = m;
        }
        return m;
    }
    /**
     * 记分板条目名必须用 NMS {@code Entity#getScoreboardName()}（Paper
     * Improve-scoreboard-entries.patch 六个方法统一用它）：玩家是用户名，其余实体是 UUID 串。
     * 原先 CraftTeam 侧写死 UUID 串、CraftScoreboard 侧写 Bukkit {@code Entity#getName()}
     * （非玩家返回 "entity.minecraft.xxx" 翻译键），两边键不一致 ——
     * addEntity() 之后 getEntityTeam() 恒返回 null（A4-5 探针 P11b 实测）。
     */
    @Unique
    private static String paperarc$scoreboardName(org.bukkit.entity.Entity entity) {
        return ((org.bukkit.craftbukkit.v.entity.CraftEntity) entity).getHandle().getScoreboardName();
    }

}
