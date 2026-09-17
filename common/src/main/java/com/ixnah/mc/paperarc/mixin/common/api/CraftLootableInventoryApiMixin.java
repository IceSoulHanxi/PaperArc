package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import com.ixnah.mc.paperarc.bridge.craft.PaperarcLootableData;
import org.bukkit.craftbukkit.v.block.CraftLootable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code LootableBlockInventory}（B3-2）在方块侧的实现体。
 *
 * <p>Barrel/Chest/Crafter/Dispenser/Dropper/Hopper/ShulkerBox 七个方块状态类都继承
 * {@code CraftLootable}，挂在这个公共基类上一次覆盖全部；接口自带的 {@code getBlock()}
 * 运行时本来就有（{@code BlockState} 上就有），不需要补。
 * 状态与语义差异见 {@link PaperarcLootableData}。
 */
@Mixin(CraftLootable.class)
public abstract class CraftLootableInventoryApiMixin {

    /** 键必须是世界里的那个方块实体，不能是每次 getState() 新建的快照。 */
    @Unique
    private Object paperarc$lootOwner() {
        return ((CraftBlockEntityStateBridge) (Object) this).paperarc$getTileEntity();
    }

    @Unique
    private PaperarcLootableData paperarc$lootData() {
        return PaperarcLootableData.of(paperarc$lootOwner());
    }

    @Unique
    public boolean isRefillEnabled() {
        return paperarc$lootData().isRefillEnabled();
    }

    @Unique
    public boolean hasBeenFilled() {
        return paperarc$lootData().hasBeenFilled();
    }

    @Unique
    public boolean canPlayerLoot(java.util.UUID player) {
        return paperarc$lootData().canPlayerLoot(player);
    }

    @Unique
    public boolean hasPlayerLooted(java.util.UUID player) {
        return paperarc$lootData().hasPlayerLooted(player);
    }

    @Unique
    public Long getLastLooted(java.util.UUID player) {
        return paperarc$lootData().getLastLooted(player);
    }

    @Unique
    public boolean setHasPlayerLooted(java.util.UUID player, boolean looted) {
        return paperarc$lootData().setHasPlayerLooted(player, looted);
    }

    @Unique
    public boolean hasPendingRefill() {
        return paperarc$lootData().hasPendingRefill();
    }

    @Unique
    public long getLastFilled() {
        return paperarc$lootData().getLastFilled();
    }

    @Unique
    public long getNextRefill() {
        return paperarc$lootData().getNextRefill();
    }

    @Unique
    public long setNextRefill(long refillAt) {
        return paperarc$lootData().setNextRefill(refillAt);
    }
}
