package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.PaperarcLootableData;
import com.ixnah.mc.paperarc.bridge.craft.CraftEntityBridge;
import java.util.UUID;
import org.bukkit.craftbukkit.v.entity.CraftChestBoat;
import org.bukkit.craftbukkit.v.entity.CraftMinecartContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code com.destroystokyo.paper.loottable.LootableEntityInventory} 的终端方法（A4-3）。
 *
 * <p>ChestBoat / StorageMinecart / HopperMinecart 三个接口 extends
 * {@code LootableEntityInventory}；运行时实现类分属两支互不继承的家族
 * （{@code CraftChestBoat} 与 {@code CraftMinecartContainer}），用多目标 mixin 一次覆盖。
 * 状态后端与方块侧共用（{@link PaperarcLootableData}），挂在 NMS 实体上。</p>
 */
@Mixin({CraftMinecartContainer.class, CraftChestBoat.class})
public abstract class CraftLootableEntityApiMixin {

    @Unique
    private PaperarcLootableData paperarc$lootData() {
        return PaperarcLootableData.of(((CraftEntityBridge) (Object) this).paperarc$getHandle());
    }

    @Unique
    public org.bukkit.entity.Entity getEntity() {
        return (org.bukkit.entity.Entity) (Object) this;
    }

    @Unique
    public boolean isRefillEnabled() {
        return this.paperarc$lootData().isRefillEnabled();
    }

    @Unique
    public boolean hasBeenFilled() {
        return this.paperarc$lootData().hasBeenFilled();
    }

    @Unique
    public boolean canPlayerLoot(UUID player) {
        return this.paperarc$lootData().canPlayerLoot(player);
    }

    @Unique
    public boolean hasPlayerLooted(UUID player) {
        return this.paperarc$lootData().hasPlayerLooted(player);
    }

    @Unique
    public Long getLastLooted(UUID player) {
        return this.paperarc$lootData().getLastLooted(player);
    }

    @Unique
    public boolean setHasPlayerLooted(UUID player, boolean looted) {
        return this.paperarc$lootData().setHasPlayerLooted(player, looted);
    }

    @Unique
    public boolean hasPendingRefill() {
        return this.paperarc$lootData().hasPendingRefill();
    }

    @Unique
    public long getLastFilled() {
        return this.paperarc$lootData().getLastFilled();
    }

    @Unique
    public long getNextRefill() {
        return this.paperarc$lootData().getNextRefill();
    }

    @Unique
    public long setNextRefill(long refillAt) {
        return this.paperarc$lootData().setNextRefill(refillAt);
    }
}
