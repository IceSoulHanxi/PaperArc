package com.ixnah.mc.paperarc.bridge.craft;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Paper 的 {@code PaperLootableInventoryData} 的等价实现，支撑
 * {@code com.destroystokyo.paper.loottable.LootableInventory} 那组 API。
 *
 * <p>按 <b>NMS 容器对象</b>（方块实体 / 矿车 / 箱船）弱引用存一份状态：Bukkit 侧的
 * {@code CraftLootable} 是每次 {@code getState()} 新建的快照包装，用它当键会立刻丢状态。
 *
 * <p><b>与 Paper 的差异（都记在 docs/gaps.md）：</b>
 * <ul>
 *   <li>不做 NBT 持久化 —— Paper 把 {@code Paper.LootableData} 写进方块实体/实体的 NBT，
 *       我们没有改 NMS 存档格式的通道，服务器重启或区块卸载后状态归零；</li>
 *   <li>{@code isRefillEnabled()} 恒为 {@code false} —— 自动补货是 paper-world.yml 的
 *       {@code lootables.auto-replenish} 特性，Arclight 没有这套配置，默认值本身也是关；</li>
 *   <li>{@code canPlayerLoot()} 恒为 {@code true} —— 同理，对应
 *       {@code lootables.restrict-player-reloot} 的默认值（关）。</li>
 * </ul>
 * 其余方法（lastFilled / nextRefill / 已搜刮玩家表）语义与 Paper 一致，
 * {@code lastFilled} 与"哪个玩家搜刮过"由 {@code LootTableFillMixin} 在真的开箱填充时记录。
 */
public final class PaperarcLootableData {

    private static final Map<Object, PaperarcLootableData> BY_OWNER = new WeakHashMap<>();

    private long lastFill = -1L;
    private long nextRefill = -1L;
    private Map<UUID, Long> lootedPlayers;

    private PaperarcLootableData() {
    }

    /** owner 为 null（拿到的是无宿主的快照）时返回一份一次性实例，读到的都是默认值。 */
    public static PaperarcLootableData of(Object owner) {
        if (owner == null) {
            return new PaperarcLootableData();
        }
        synchronized (BY_OWNER) {
            return BY_OWNER.computeIfAbsent(owner, k -> new PaperarcLootableData());
        }
    }

    /** vanilla 真的用战利品表填充容器时调用（{@code LootTable#fill}）。 */
    public void recordFill(UUID looter) {
        this.lastFill = System.currentTimeMillis();
        if (looter != null) {
            setPlayerLootedState(looter, true);
        }
    }

    public boolean isRefillEnabled() {
        return false;
    }

    public boolean hasBeenFilled() {
        return this.lastFill != -1L;
    }

    public boolean canPlayerLoot(UUID player) {
        return true;
    }

    public boolean hasPlayerLooted(UUID player) {
        return this.lootedPlayers != null && this.lootedPlayers.containsKey(player);
    }

    public Long getLastLooted(UUID player) {
        return this.lootedPlayers == null ? null : this.lootedPlayers.get(player);
    }

    /** 返回**改动前**的状态，与 Paper 一致。 */
    public boolean setHasPlayerLooted(UUID player, boolean looted) {
        boolean had = hasPlayerLooted(player);
        if (had != looted) {
            setPlayerLootedState(player, looted);
        }
        return had;
    }

    public boolean hasPendingRefill() {
        return this.nextRefill != -1L && this.nextRefill > this.lastFill;
    }

    public long getLastFilled() {
        return this.lastFill;
    }

    public long getNextRefill() {
        return this.nextRefill;
    }

    public long setNextRefill(long refillAt) {
        long prev = this.nextRefill;
        this.nextRefill = Math.max(refillAt, -1L);
        return prev;
    }

    private void setPlayerLootedState(UUID player, boolean looted) {
        if (looted) {
            if (this.lootedPlayers == null) {
                this.lootedPlayers = new HashMap<>();
            }
            this.lootedPlayers.put(player, System.currentTimeMillis());
        } else if (this.lootedPlayers != null) {
            this.lootedPlayers.remove(player);
        }
    }
}
