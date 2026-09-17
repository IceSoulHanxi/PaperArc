package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.PaperarcLootableData;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import java.util.UUID;
import org.bukkit.craftbukkit.v.block.CraftLootable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code com.destroystokyo.paper.loottable.LootableBlockInventory} 的终端方法（A4-3）。
 *
 * <p>Barrel/Chest/Dispenser/Dropper/Hopper/ShulkerBox 六个接口都 extends
 * {@code LootableBlockInventory}，对应的 Craft 类都继承 {@code CraftLootable}，
 * 所以实现体挂在这一个公共宿主上。{@code getBlock()} 运行时（CraftBlockState）已有。</p>
 *
 * <p>注意不要碰 {@code setLootTable}：{@code CraftLootable} 上有一个 private 的
 * 二参重载，同签名的 public 方法会把它替换掉并造成无限递归（docs/mixin-conventions.md）。</p>
 */
@Mixin(CraftLootable.class)
public abstract class CraftLootableBlockApiMixin {

    @Unique
    private PaperarcLootableData paperarc$lootData() {
        Object owner = ((CraftBlockEntityStateBridge) (Object) this).paperarc$getTileEntityFromWorld();
        return PaperarcLootableData.of(owner != null ? owner : this);
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
