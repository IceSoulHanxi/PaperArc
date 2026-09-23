package com.ixnah.mc.paperarc.bridge.craft;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Paper 的 {@code PaperLootableInventoryData} 的等价实现，支撑
 * {@code com.destroystokyo.paper.loottable.LootableInventory} 那组 API。
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

    private static final String NBT_KEY = "Paper.LootableData";
    private static final String NBT_LAST_FILL = "lastFill";
    private static final String NBT_NEXT_REFILL = "nextRefill";
    private static final String NBT_LOOTED = "lootedPlayers";
    private static final String NBT_UUID = "UUID";
    private static final String NBT_TIME = "Time";

    public static void saveIfPresent(Object owner, ValueOutput out) {
        PaperarcLootableData data;
        synchronized (BY_OWNER) {
            data = BY_OWNER.get(owner);
        }
        if (data == null || data.isEmpty()) {
            return;
        }
        ValueOutput child = out.child(NBT_KEY);
        child.putLong(NBT_LAST_FILL, data.lastFill);
        child.putLong(NBT_NEXT_REFILL, data.nextRefill);
        if (data.lootedPlayers != null && !data.lootedPlayers.isEmpty()) {
            ValueOutput.ValueOutputList list = child.childrenList(NBT_LOOTED);
            data.lootedPlayers.forEach((uuid, time) -> {
                ValueOutput entry = list.addChild();
                entry.store(NBT_UUID, net.minecraft.core.UUIDUtil.CODEC, uuid);
                entry.putLong(NBT_TIME, time);
            });
        }
    }

    public static void loadIfPresent(Object owner, ValueInput in) {
        in.child(NBT_KEY).ifPresent(child -> {
            PaperarcLootableData data = of(owner);
            data.lastFill = child.getLongOr(NBT_LAST_FILL, -1L);
            data.nextRefill = child.getLongOr(NBT_NEXT_REFILL, -1L);
            data.lootedPlayers = null;
            child.childrenList(NBT_LOOTED).ifPresent(list -> {
                list.stream().forEach(entry -> {
                    entry.read(NBT_UUID, net.minecraft.core.UUIDUtil.CODEC).ifPresent(uuid -> {
                        if (data.lootedPlayers == null) {
                            data.lootedPlayers = new HashMap<>();
                        }
                        data.lootedPlayers.put(uuid, entry.getLongOr(NBT_TIME, 0L));
                    });
                });
            });
        });
    }

    private boolean isEmpty() {
        return this.lastFill == -1L && this.nextRefill == -1L
                && (this.lootedPlayers == null || this.lootedPlayers.isEmpty());
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
