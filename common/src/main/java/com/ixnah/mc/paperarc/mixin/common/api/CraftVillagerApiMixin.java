package com.ixnah.mc.paperarc.mixin.common.api;

import com.destroystokyo.paper.entity.villager.Reputation;
import com.destroystokyo.paper.entity.villager.ReputationType;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import org.bukkit.craftbukkit.v.entity.CraftVillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.EnumMap;
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
 *   <li>Reputation API → 读写 NMS {@code Villager#getGossips()} 的真实
 *       {@code GossipContainer}（与 Paper 的 Add-villager-reputation-API 一致）。
 *       <b>B8/Y-4 改</b>：原实现是在 Craft 侧注入一个 map —— 既不影响村民真实声望
 *       （交易折扣、铁傀儡敌意都看 GossipContainer），也不随实体持久化，
 *       属"存了个值没人读"（checklist §1.10 an）。</li>
 * </ul>
 */
@Mixin(CraftVillager.class)
public abstract class CraftVillagerApiMixin {

    @Shadow
    public abstract Villager getHandle();

    @Unique
    private static final GossipType[] PAPERARC$GOSSIP_TYPES = GossipType.values();

    @Unique
    public boolean addTrades(int amount) {
        // 参考 Paper：updateTrades() 会向当前 offers 列表按当前等级追加新交易，
        // 数量参数由 Paper 的补丁用于控制追加数量；vanilla 的 updateTrades()
        // 无数量参数，这里等价于追加一批当前等级可解锁的新交易。
        if (!(this.getHandle().level() instanceof net.minecraft.server.level.ServerLevel level)) {
            return false;
        }
        this.getHandle().updateTrades(level);
        return true;
    }

    @Unique
    public boolean increaseLevel(int amount) {
        Preconditions.checkArgument(amount > 0, "Amount must be greater than 0");
        Villager handle = this.getHandle();
        VillagerData villagerData = handle.getVillagerData();
        if (villagerData.level() >= VillagerData.MAX_VILLAGER_LEVEL) {
            return false;
        }
        handle.setVillagerData(villagerData.withLevel(
            Math.min(villagerData.level() + amount, VillagerData.MAX_VILLAGER_LEVEL)));
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
    private static ReputationType paperarc$toApi(GossipType type) {
        return switch (type) {
            case MAJOR_NEGATIVE -> ReputationType.MAJOR_NEGATIVE;
            case MINOR_NEGATIVE -> ReputationType.MINOR_NEGATIVE;
            case MINOR_POSITIVE -> ReputationType.MINOR_POSITIVE;
            case MAJOR_POSITIVE -> ReputationType.MAJOR_POSITIVE;
            case TRADING -> ReputationType.TRADING;
        };
    }

    @Unique
    private static Reputation paperarc$toReputation(Object2IntMap<GossipType> entries) {
        Map<ReputationType, Integer> map = new EnumMap<>(ReputationType.class);
        if (entries != null) {
            for (GossipType type : PAPERARC$GOSSIP_TYPES) {
                if (entries.containsKey(type)) {
                    map.put(paperarc$toApi(type), entries.getInt(type));
                }
            }
        }
        return new Reputation(map);
    }

    @Unique
    public Map<UUID, Reputation> getReputations() {
        Map<UUID, Reputation> out = new HashMap<>();
        for (Map.Entry<UUID, Object2IntMap<GossipType>> entry
                : this.getHandle().getGossips().getGossipEntries().entrySet()) {
            out.put(entry.getKey(), paperarc$toReputation(entry.getValue()));
        }
        return out;
    }

    @Unique
    public Reputation getReputation(UUID uniqueId) {
        Preconditions.checkArgument(uniqueId != null, "uniqueId cannot be null");
        return paperarc$toReputation(
                this.getHandle().getGossips().getGossipEntries().get(uniqueId));
    }

    /**
     * NMS 只有"累加"语义的 {@code add}，所以先 {@code remove} 清零再 {@code add}。
     * 与 Paper 的偏差：{@code GossipContainer#add} 会按 {@code GossipType.max} 截顶
     * （Paper 直写 entries 不截），超过上限的值会被夹到上限。
     */
    @Unique
    public void setReputation(UUID uniqueId, Reputation reputation) {
        Preconditions.checkArgument(uniqueId != null, "uniqueId cannot be null");
        Preconditions.checkArgument(reputation != null, "reputation cannot be null");
        GossipContainer gossips = this.getHandle().getGossips();
        for (GossipType type : PAPERARC$GOSSIP_TYPES) {
            ReputationType api = paperarc$toApi(type);
            if (!reputation.hasReputationSet(api)) {
                continue;
            }
            int value = reputation.getReputation(api);
            gossips.remove(uniqueId, type);
            if (value != 0) {
                gossips.add(uniqueId, type, value);
            }
        }
    }

    @Unique
    public void setReputations(Map<UUID, Reputation> reputations) {
        Preconditions.checkArgument(reputations != null, "reputations cannot be null");
        for (Map.Entry<UUID, Reputation> entry : reputations.entrySet()) {
            this.setReputation(entry.getKey(), entry.getValue());
        }
    }

    @Unique
    public void clearReputations() {
        GossipContainer gossips = this.getHandle().getGossips();
        for (GossipType type : PAPERARC$GOSSIP_TYPES) {
            gossips.remove(type);
        }
    }
}
