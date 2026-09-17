package com.ixnah.mc.paperarc.bridge;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * {@code com.destroystokyo.paper.loottable.LootableInventory} 的状态后端（A4-3）。
 *
 * <p>Paper 把这套状态存在 tile entity / 实体的 {@code PaperLootableInventoryData} 里，
 * 并由 paper.yml 的 loot-table 自动补货配置驱动。Arclight 没有那套配置与补货管线，
 * 因此这里只提供「谁看过这个容器」的记账，自动补货一律报告为关闭
 * （{@code isRefillEnabled()} 恒 false、{@code getNextRefill()} 恒 -1）。
 * 语义差异见 docs/gaps.md。</p>
 *
 * <p>状态挂在 NMS 宿主对象上（方块是 tile entity，实体是 NMS entity），
 * 走 {@link ApiState}，随宿主一起回收。</p>
 */
public final class PaperarcLootableData {

    private static final String KEY = "paperarc:lootable";

    private final Map<UUID, Long> looted = new HashMap<>();
    private long lastFilled = -1L;
    private long nextRefill = -1L;

    private PaperarcLootableData() {
    }

    public static PaperarcLootableData of(Object owner) {
        PaperarcLootableData data = ApiState.get(owner, KEY, null);
        if (data == null) {
            data = new PaperarcLootableData();
            ApiState.put(owner, KEY, data);
        }
        return data;
    }

    public boolean isRefillEnabled() {
        return false;
    }

    public boolean hasBeenFilled() {
        return this.lastFilled != -1L;
    }

    public boolean canPlayerLoot(UUID player) {
        return !this.hasPlayerLooted(player);
    }

    public boolean hasPlayerLooted(UUID player) {
        return player != null && this.looted.containsKey(player);
    }

    public Long getLastLooted(UUID player) {
        return player == null ? null : this.looted.get(player);
    }

    public boolean setHasPlayerLooted(UUID player, boolean looted) {
        if (player == null) {
            return false;
        }
        boolean had = this.looted.containsKey(player);
        if (looted) {
            this.looted.put(player, System.currentTimeMillis());
        } else {
            this.looted.remove(player);
        }
        return had;
    }

    public boolean hasPendingRefill() {
        return false;
    }

    public long getLastFilled() {
        return this.lastFilled;
    }

    public long getNextRefill() {
        return this.nextRefill;
    }

    public long setNextRefill(long refillAt) {
        long old = this.nextRefill;
        this.nextRefill = refillAt;
        return old;
    }
}
