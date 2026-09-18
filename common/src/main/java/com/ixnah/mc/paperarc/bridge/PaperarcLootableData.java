package com.ixnah.mc.paperarc.bridge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

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

    // ---- NBT 落盘（A8/Y-3）----
    //
    // Paper 把这块状态写在 `Paper.LootableData` 复合标签里（`lastFill`/`nextRefill`/
    // `lootedPlayers`），宿主是容器方块实体与载具实体。这里沿用同一套键名，
    // 只在真有记账时才写，避免给每个方块实体/实体都多一个空标签。

    private static final String NBT_KEY = "Paper.LootableData";
    private static final String NBT_LAST_FILL = "lastFill";
    private static final String NBT_NEXT_REFILL = "nextRefill";
    private static final String NBT_LOOTED = "lootedPlayers";
    private static final String NBT_UUID = "UUID";
    private static final String NBT_TIME = "Time";

    /** 宿主还没有过任何 lootable 记账时不落盘（绝大多数方块实体/实体都走这条）。 */
    public static void saveIfPresent(Object owner, CompoundTag nbt) {
        PaperarcLootableData data = ApiState.get(owner, KEY, null);
        if (data == null || data.isEmpty()) {
            return;
        }
        nbt.put(NBT_KEY, data.save());
    }

    public static void loadIfPresent(Object owner, CompoundTag nbt) {
        if (!nbt.contains(NBT_KEY, Tag.TAG_COMPOUND)) {
            return;
        }
        of(owner).load(nbt.getCompound(NBT_KEY));
    }

    private boolean isEmpty() {
        return this.looted.isEmpty() && this.lastFilled == -1L && this.nextRefill == -1L;
    }

    private CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong(NBT_LAST_FILL, this.lastFilled);
        tag.putLong(NBT_NEXT_REFILL, this.nextRefill);
        ListTag list = new ListTag();
        this.looted.forEach((uuid, time) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID(NBT_UUID, uuid);
            entry.putLong(NBT_TIME, time);
            list.add(entry);
        });
        tag.put(NBT_LOOTED, list);
        return tag;
    }

    private void load(CompoundTag tag) {
        this.lastFilled = tag.contains(NBT_LAST_FILL) ? tag.getLong(NBT_LAST_FILL) : -1L;
        this.nextRefill = tag.contains(NBT_NEXT_REFILL) ? tag.getLong(NBT_NEXT_REFILL) : -1L;
        this.looted.clear();
        ListTag list = tag.getList(NBT_LOOTED, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (entry.hasUUID(NBT_UUID)) {
                this.looted.put(entry.getUUID(NBT_UUID), entry.getLong(NBT_TIME));
            }
        }
    }
}
