package com.ixnah.mc.paperarc.mixin.common.api;

import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.EntityCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;

/**
 * paper 加在 {@code EnchantmentWrapper} 上的 9 个方法（checklist §1.10 am，第 ⑤ 批）：
 * 每个都只是转调被包装的 {@code getEnchantment()}，被转调的那一侧由
 * {@code bukkit.EnchantmentIfaceMixin} + {@code api.CraftEnchantmentApiMixin} 提供。
 */
@Mixin(EnchantmentWrapper.class)
public abstract class EnchantmentWrapperApiMixin {

    @Unique
    private EnchantmentWrapper paperarc$self() {
        return (EnchantmentWrapper) (Object) this;
    }

    @Unique
    public Component displayName(int level) {
        return this.paperarc$self().getEnchantment().displayName(level);
    }

    @Unique
    public String translationKey() {
        return this.paperarc$self().getEnchantment().translationKey();
    }

    @Unique
    public boolean isTradeable() {
        return this.paperarc$self().getEnchantment().isTradeable();
    }

    @Unique
    public boolean isDiscoverable() {
        return this.paperarc$self().getEnchantment().isDiscoverable();
    }

    @Unique
    public int getMinModifiedCost(int level) {
        return this.paperarc$self().getEnchantment().getMinModifiedCost(level);
    }

    @Unique
    public int getMaxModifiedCost(int level) {
        return this.paperarc$self().getEnchantment().getMaxModifiedCost(level);
    }

    @Unique
    public EnchantmentRarity getRarity() {
        return this.paperarc$self().getEnchantment().getRarity();
    }

    @Unique
    public float getDamageIncrease(int level, EntityCategory entityCategory) {
        return this.paperarc$self().getEnchantment().getDamageIncrease(level, entityCategory);
    }

    @Unique
    public Set<EquipmentSlot> getActiveSlots() {
        return this.paperarc$self().getEnchantment().getActiveSlots();
    }
}
