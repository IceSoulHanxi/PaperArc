package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v.inventory.CraftInventoryPlayer;
import org.bukkit.inventory.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code EntityEquipment#getDropChance/setDropChance} 的第二个实现类：
 * 玩家背包 {@code CraftInventoryPlayer} 也实现 {@code EntityEquipment}，
 * 与 {@code CraftEntityEquipment} 互不继承（PARTIAL_IMPL 门禁抓到）。
 *
 * <p>玩家没有"掉落概率"这回事（死亡掉落由 keepInventory / 死亡事件决定），
 * 与 Paper 一致：getter 恒 1，setter 抛 UnsupportedOperationException。</p>
 */
@Mixin(CraftInventoryPlayer.class)
public abstract class CraftInventoryPlayerEquipmentApiMixin {

    @Unique
    public float getDropChance(EquipmentSlot slot) {
        Preconditions.checkArgument(slot != null, "slot cannot be null");
        CraftEquipmentSlot.getNMS(slot);
        return 1.0F;
    }

    @Unique
    public void setDropChance(EquipmentSlot slot, float chance) {
        throw new UnsupportedOperationException("Cannot set drop chance for players");
    }
}
