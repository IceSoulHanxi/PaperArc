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
}
