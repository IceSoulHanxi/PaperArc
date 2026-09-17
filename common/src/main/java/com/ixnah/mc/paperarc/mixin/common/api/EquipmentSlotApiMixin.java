package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.inventory.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 加在 {@code EquipmentSlot} 枚举上的两个便利方法，运行时没有（B3-3 探针 P20 实测
 * {@code NoSuchMethodError}）。{@code LivingEntity#swingHand} 这类 default 方法体里就在用它，
 * 不补上等于那条 default 方法一调就炸。
 *
 * <p>枚举是普通类，不是接口，所以走 class mixin 而不是 IfaceMixin；
 * {@code audit.py} 只比对接口，这类缺口它看不见。
 */
@Mixin(EquipmentSlot.class)
public abstract class EquipmentSlotApiMixin {

    @Unique
    public boolean isHand() {
        EquipmentSlot self = (EquipmentSlot) (Object) this;
        return self == EquipmentSlot.HAND || self == EquipmentSlot.OFF_HAND;
    }

    @Unique
    public EquipmentSlot getOppositeHand() {
        EquipmentSlot self = (EquipmentSlot) (Object) this;
        return switch (self) {
            case HAND -> EquipmentSlot.OFF_HAND;
            case OFF_HAND -> EquipmentSlot.HAND;
            default -> throw new IllegalArgumentException("Cannot get opposite hand of " + self);
        };
    }
}
