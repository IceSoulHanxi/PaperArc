package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.PaperarcLootableData;
import org.bukkit.craftbukkit.v.entity.CraftChestBoat;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code LootableEntityInventory}（B3-2）在箱船侧的实现体。
 * {@code CraftChestBoat} 继承 {@code CraftBoat}，与矿车那条继承链互不相干，
 * 只覆盖一边就是 {@code AbstractMethodError}（checklist §1.6 r）。
 */
@Mixin(CraftChestBoat.class)
public abstract class CraftChestBoatLootableApiMixin {

    @Unique
    private Object paperarc$lootOwner() {
        return ((CraftEntity) (Object) this).getHandle();
    }

    @Unique
    public org.bukkit.entity.Entity getEntity() {
        return (org.bukkit.entity.Entity) (Object) this;
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
