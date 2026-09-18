package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@link org.bukkit.enchantments.Enchantment} 是**抽象类**不是接口，用类目标 mixin 加
 * {@code @Unique public abstract} 声明；实现体在 {@code api.CraftEnchantmentApiMixin}
 * （唯一的运行时子类 CraftEnchantment）。
 * Adds paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).
 */
@Mixin(targets = "org.bukkit.enchantments.Enchantment", remap = false)
public abstract class EnchantmentIfaceMixin {

    @Unique
    public abstract net.kyori.adventure.text.Component description();

    @Unique
    public abstract net.kyori.adventure.text.Component displayName(int p0);

    @Unique
    public abstract java.util.Set getActiveSlotGroups();

    @Unique
    public abstract int getAnvilCost();

    @Unique
    public abstract float getDamageIncrease(int p0, org.bukkit.entity.EntityCategory p1);

    @Unique
    public abstract float getDamageIncrease(int p0, org.bukkit.entity.EntityType p1);

    @Unique
    public abstract io.papermc.paper.registry.set.RegistryKeySet getExclusiveWith();

    @Unique
    public abstract int getMaxModifiedCost(int p0);

    @Unique
    public abstract int getMinModifiedCost(int p0);

    @Unique
    public abstract io.papermc.paper.registry.set.RegistryKeySet getPrimaryItems();

    @Unique
    public abstract io.papermc.paper.enchantments.EnchantmentRarity getRarity();

    @Unique
    public abstract io.papermc.paper.registry.set.RegistryKeySet getSupportedItems();

    @Unique
    public abstract int getWeight();

    @Unique
    public abstract boolean isDiscoverable();

    @Unique
    public abstract boolean isTradeable();

    @Unique
    public abstract java.lang.String translationKey();

    /**
     * paper 把它写成了具体方法（按 {@code getActiveSlotGroups()} 过滤
     * {@code EquipmentSlot.values()}），这里同样写具体实现，不需要再落一层 Craft 实现。
     */
    @Unique
    public java.util.Set<org.bukkit.inventory.EquipmentSlot> getActiveSlots() {
        java.util.Set<org.bukkit.inventory.EquipmentSlotGroup> groups =
                ((org.bukkit.enchantments.Enchantment) (Object) this).getActiveSlotGroups();
        java.util.Set<org.bukkit.inventory.EquipmentSlot> out =
                new java.util.LinkedHashSet<>();
        for (org.bukkit.inventory.EquipmentSlot slot : org.bukkit.inventory.EquipmentSlot.values()) {
            for (org.bukkit.inventory.EquipmentSlotGroup group : groups) {
                if (group.test(slot)) {
                    out.add(slot);
                    break;
                }
            }
        }
        return out;
    }
}
