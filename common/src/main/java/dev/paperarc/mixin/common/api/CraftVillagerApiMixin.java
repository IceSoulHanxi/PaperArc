package dev.paperarc.mixin.common.api;

import com.destroystokyo.paper.entity.villager.Reputation;
import com.google.common.base.Preconditions;
import dev.paperarc.bridge.ApiState;
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
 * <p>Mappings to this codebase's NMS (mojmap 1.21.1):
 * <ul>
 *   <li>{@code getRestocksToday()} / {@code setRestocksToday(int)} → private
 *       field {@code Villager#numberOfRestocksToday} (@Shadow).</li>
 *   <li>{@code addTrades(int)} → protected {@code Villager#updateTrades()}
 *       (@Shadow), which appends level-appropriate trades to the current
 *       offers list, mirroring Paper's approach.</li>
 *   <li>{@code increaseLevel(int)} → {@link VillagerData} level bump with the
 *       same bounds Paper uses ({@code VillagerData.MAX_VILLAGER_LEVEL}).</li>
 *   <li>Reputation API → Paper stores per-CraftVillager
 *       {@code Map<UUID, Reputation>} in a CB-side field; vanilla NMS has no
 *       storage for it, so the map is kept in {@link ApiState} keyed by the NMS
 *       handle (side-map).</li>
 * </ul>
 */
@Mixin(CraftVillager.class)
public abstract class CraftVillagerApiMixin {

    @Shadow
    public abstract Villager getHandle();

    @Shadow
    protected abstract void updateTrades();

    @Shadow
    private int numberOfRestocksToday;

    @Unique
    private static final String PAPERARC$KEY_REPUTATIONS = "villager$reputations";

    @Unique
    public boolean addTrades(int amount) {
        // 参考 Paper：updateTrades() 会向当前 offers 列表按当前等级追加新交易，
        // 数量参数由 Paper 的补丁用于控制追加数量；vanilla 的 updateTrades()
        // 无数量参数，这里等价于追加一批当前等级可解锁的新交易。
        this.updateTrades();
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
        return this.numberOfRestocksToday;
    }

    @Unique
    public void setRestocksToday(int restocksToday) {
        this.numberOfRestocksToday = restocksToday;
    }

    @Unique
    public Map<UUID, Reputation> getReputations() {
        Map<UUID, Reputation> reputations = paperarc$reputations(false);
        return reputations == null ? new HashMap<>() : new HashMap<>(reputations);
    }

    @Unique
    public Reputation getReputation(UUID uniqueId) {
        Map<UUID, Reputation> reputations = paperarc$reputations(false);
        return reputations == null ? null : reputations.get(uniqueId);
    }

    @Unique
    public void setReputation(UUID uniqueId, Reputation reputation) {
        Preconditions.checkArgument(uniqueId != null, "uniqueId cannot be null");
        Preconditions.checkArgument(reputation != null, "reputation cannot be null");
        paperarc$reputations(true).put(uniqueId, reputation);
    }

    @Unique
    public void setReputations(Map<UUID, Reputation> reputations) {
        Preconditions.checkArgument(reputations != null, "reputations cannot be null");
        if (reputations.isEmpty()) {
            ApiState.remove(this.getHandle(), PAPERARC$KEY_REPUTATIONS);
        } else {
            ApiState.put(this.getHandle(), PAPERARC$KEY_REPUTATIONS, new HashMap<>(reputations));
        }
    }

    @Unique
    public void clearReputations() {
        ApiState.remove(this.getHandle(), PAPERARC$KEY_REPUTATIONS);
    }

    /**
     * Side-map accessor: Paper keeps a per-CraftVillager
     * {@code Map<UUID, Reputation>} field; here it lives in {@link ApiState}
     * keyed by the NMS villager handle so it dies with the entity.
     */
    @Unique
    private Map<UUID, Reputation> paperarc$reputations(boolean createIfAbsent) {
        Map<UUID, Reputation> map =
            ApiState.get(this.getHandle(), PAPERARC$KEY_REPUTATIONS, (Map<UUID, Reputation>) null);
        if (map == null && createIfAbsent) {
            map = new HashMap<>();
            ApiState.put(this.getHandle(), PAPERARC$KEY_REPUTATIONS, map);
        }
        return map;
    }
}
