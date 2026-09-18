package com.ixnah.mc.paperarc.bridge.craft;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

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
 *   <li>~~不做 NBT 持久化~~ —— <b>B8/Y-3 已补</b>：与 Paper 用同一组键
 *       （{@code Paper.LootableData} / {@code lastFill} / {@code nextRefill} /
 *       {@code lootedPlayers}）写进方块实体与实体的 NBT，见 {@link #saveIfPresent}；</li>
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

    // ---- NBT 落盘（B8/Y-3）----
    //
    // 键名与 Paper 的 PaperLootableInventoryData 一致，方便与真 Paper 存档互读。
    // 只在真有记账时才写：绝大多数方块实体/实体一辈子也不会碰这套状态，
    // 给它们都塞一个空标签既浪费存档也拖慢区块保存。

    private static final String NBT_KEY = "Paper.LootableData";
    private static final String NBT_LAST_FILL = "lastFill";
    private static final String NBT_NEXT_REFILL = "nextRefill";
    private static final String NBT_LOOTED = "lootedPlayers";
    private static final String NBT_UUID = "UUID";
    private static final String NBT_TIME = "Time";

    public static void saveIfPresent(Object owner, CompoundTag nbt) {
        PaperarcLootableData data;
        synchronized (BY_OWNER) {
            data = BY_OWNER.get(owner);
        }
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
        return this.lastFill == -1L && this.nextRefill == -1L
                && (this.lootedPlayers == null || this.lootedPlayers.isEmpty());
    }

    private CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong(NBT_LAST_FILL, this.lastFill);
        tag.putLong(NBT_NEXT_REFILL, this.nextRefill);
        ListTag list = new ListTag();
        if (this.lootedPlayers != null) {
            this.lootedPlayers.forEach((uuid, time) -> {
                CompoundTag entry = new CompoundTag();
                entry.putUUID(NBT_UUID, uuid);
                entry.putLong(NBT_TIME, time);
                list.add(entry);
            });
        }
        tag.put(NBT_LOOTED, list);
        return tag;
    }

    private void load(CompoundTag tag) {
        this.lastFill = tag.contains(NBT_LAST_FILL) ? tag.getLong(NBT_LAST_FILL) : -1L;
        this.nextRefill = tag.contains(NBT_NEXT_REFILL) ? tag.getLong(NBT_NEXT_REFILL) : -1L;
        this.lootedPlayers = null;
        ListTag list = tag.getList(NBT_LOOTED, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (entry.hasUUID(NBT_UUID)) {
                if (this.lootedPlayers == null) {
                    this.lootedPlayers = new HashMap<>();
                }
                this.lootedPlayers.put(entry.getUUID(NBT_UUID), entry.getLong(NBT_TIME));
            }
        }
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
