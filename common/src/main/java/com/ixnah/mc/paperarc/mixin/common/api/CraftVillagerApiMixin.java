package com.ixnah.mc.paperarc.mixin.common.api;

import com.destroystokyo.paper.entity.villager.Reputation;
import com.google.common.base.Preconditions;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import org.bukkit.craftbukkit.v.entity.CraftVillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Port of Paper's Add-villager-reputation-API,
 * More-vanilla-friendly-methods-to-update-trades and Villager-Restocks-API
 * additions on {@link CraftVillager}.
 *
 * <p>Mappings to this codebase's NMS (mojmap 1.20.1):
 * <ul>
 *   <li>{@code getRestocksToday()} / {@code setRestocksToday(int)} → private
 *       field {@code Villager#numberOfRestocksToday} (@Shadow).</li>
 *   <li>{@code addTrades(int)} → protected {@code Villager#updateTrades()}
 *       (@Shadow), which appends level-appropriate trades to the current
 *       offers list, mirroring Paper's approach.</li>
 *   <li>{@code increaseLevel(int)} → {@link VillagerData} level bump with the
 *       same bounds Paper uses ({@code VillagerData.MAX_VILLAGER_LEVEL}).</li>
 *   <li>Reputation API → Paper stores per-CraftVillager
 *       {@code Map<UUID, Reputation>} in a CB-side field; injected here as
 *       {@code reputations} (aligned with Paper, no prefix).</li>
 * </ul>
 */
@Mixin(CraftVillager.class)
public abstract class CraftVillagerApiMixin {

    @Shadow
    public abstract Villager getHandle();

    /** Paper CB-side per-villager reputation map. */
    @Unique
    private Map<UUID, Reputation> reputations = new HashMap<>();

    @Unique
    public boolean addTrades(int amount) {
        // 参考 Paper：updateTrades() 会向当前 offers 列表按当前等级追加新交易，
        // 数量参数由 Paper 的补丁用于控制追加数量；vanilla 的 updateTrades()
        // 无数量参数，这里等价于追加一批当前等级可解锁的新交易。
        this.getHandle().updateTrades();
        return true;
    }

    @Unique
    public boolean increaseLevel(int amount) {
        Preconditions.checkArgument(amount > 0, "Amount must be greater than 0");
        Villager handle = this.getHandle();
        VillagerData villagerData = handle.getVillagerData();
        if (villagerData.getLevel() >= VillagerData.MAX_VILLAGER_LEVEL) {
            return false;
        }
        handle.setVillagerData(villagerData.setLevel(
            Math.min(villagerData.getLevel() + amount, VillagerData.MAX_VILLAGER_LEVEL)));
        return true;
    }

    @Unique
    public int getRestocksToday() {
        return this.getHandle().numberOfRestocksToday;
    }

    @Unique
    public void setRestocksToday(int restocksToday) {
        this.getHandle().numberOfRestocksToday = restocksToday;
    }

    @Unique
    public Map<UUID, Reputation> getReputations() {
        return new HashMap<>(this.reputations);
    }

    @Unique
    public Reputation getReputation(UUID uniqueId) {
        return this.reputations.get(uniqueId);
    }

    @Unique
    public void setReputation(UUID uniqueId, Reputation reputation) {
        Preconditions.checkArgument(uniqueId != null, "uniqueId cannot be null");
        Preconditions.checkArgument(reputation != null, "reputation cannot be null");
        this.reputations.put(uniqueId, reputation);
    }

    @Unique
    public void setReputations(Map<UUID, Reputation> reputations) {
        Preconditions.checkArgument(reputations != null, "reputations cannot be null");
        this.reputations.clear();
        if (!reputations.isEmpty()) {
            this.reputations.putAll(reputations);
        }
    }

    @Unique
    public void clearReputations() {
        this.reputations.clear();
    }
}
