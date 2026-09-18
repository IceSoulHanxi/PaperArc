package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.inventory.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 加在 {@code EquipmentSlot} 枚举上的两个便利方法，运行时没有（checklist §1.9 aj）。
 *
 * <p>枚举是普通类，不是接口，所以走 class mixin 而不是 IfaceMixin；{@code audit.py}
 * 只比对接口，这类缺口它看不见 —— 1.20.1 的清单见
 * {@code docs/data/1201-class-method-gaps.md}（脚本 {@code class-method-gaps-1201.py}）。
 *
 * <p>1.20.1 的 paper-api 是 {@code isHand}/{@code isArmor}（main 的 1.21.1 是
 * {@code isHand}/{@code getOppositeHand}，javap 核对，不能照搬）。</p>
 */
@Mixin(EquipmentSlot.class)
public abstract class EquipmentSlotApiMixin {

    @Unique
    public boolean isHand() {
        EquipmentSlot self = (EquipmentSlot) (Object) this;
        return self == EquipmentSlot.HAND || self == EquipmentSlot.OFF_HAND;
    }

    @Unique
    public boolean isArmor() {
        EquipmentSlot self = (EquipmentSlot) (Object) this;
        return self == EquipmentSlot.HEAD || self == EquipmentSlot.CHEST
                || self == EquipmentSlot.LEGS || self == EquipmentSlot.FEET;
    }
}
