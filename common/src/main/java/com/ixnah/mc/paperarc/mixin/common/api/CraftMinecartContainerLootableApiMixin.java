package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.PaperarcLootableData;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.craftbukkit.v.entity.CraftMinecartContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code LootableEntityInventory}（B3-2）在储物 / 漏斗矿车侧的实现体。
 * 两个矿车类都继承 {@code CraftMinecartContainer}，挂在基类上一次覆盖。
 */
@Mixin(CraftMinecartContainer.class)
public abstract class CraftMinecartContainerLootableApiMixin {

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
