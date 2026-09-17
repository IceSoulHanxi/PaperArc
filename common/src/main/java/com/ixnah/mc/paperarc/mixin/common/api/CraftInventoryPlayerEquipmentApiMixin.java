package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.inventory.CraftInventoryPlayer;
import org.bukkit.inventory.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code EntityEquipment} 的第二个实现类。
 *
 * <p>{@code getDropChance}/{@code setDropChance} 的实现体原先只挂在
 * {@code CraftEntityEquipment} 上，玩家背包（{@code CraftInventoryPlayer} 也实现
 * {@code EntityEquipment}）走到就是 {@code AbstractMethodError}（A4-1 r）。
 * 语义与 CB 既有的 {@code getItemInHandDropChance()} 一致：玩家没有掉落率概念。
 */
@Mixin(CraftInventoryPlayer.class)
public abstract class CraftInventoryPlayerEquipmentApiMixin {

    @Unique
    public float getDropChance(EquipmentSlot slot) {
        throw new UnsupportedOperationException("Cannot get drop chance for players");
    }

    @Unique
    public void setDropChance(EquipmentSlot slot, float chance) {
        throw new UnsupportedOperationException("Cannot set drop chance for players");
    }
}
