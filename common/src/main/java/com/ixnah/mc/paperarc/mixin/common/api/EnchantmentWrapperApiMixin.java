package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.EntityCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;

/**
 * {@code EnchantmentWrapper} 是**另一族**实现：它 {@code extends Enchantment} 但不是
 * {@code CraftEnchantment}，paper 的方法在它上面全部转发给 {@code getEnchantment()}。
 * 我们的实现体挂在 {@code CraftEnchantment} 上，wrapper 这边不补就是 {@code AbstractMethodError}
 * （§1.10 ap "Tameable 两族实现类" 的同款形态，A6/X-2 第三批）。
 */
@Mixin(EnchantmentWrapper.class)
public abstract class EnchantmentWrapperApiMixin {

    @Unique
    private org.bukkit.enchantments.Enchantment paperarc$delegate() {
        return ((EnchantmentWrapper) (Object) this).getEnchantment();
    }

    @Unique
    public Component displayName(int level) {
        return this.paperarc$delegate().displayName(level);
    }

    @Unique
    public String translationKey() {
        return this.paperarc$delegate().translationKey();
    }

    @Unique
    public boolean isTradeable() {
        return this.paperarc$delegate().isTradeable();
    }

    @Unique
    public boolean isDiscoverable() {
        return this.paperarc$delegate().isDiscoverable();
    }

    @Unique
    public io.papermc.paper.enchantments.EnchantmentRarity getRarity() {
        return this.paperarc$delegate().getRarity();
    }

    @Unique
    public float getDamageIncrease(int level, EntityCategory entityCategory) {
        return this.paperarc$delegate().getDamageIncrease(level, entityCategory);
    }

    @Unique
    public Set<EquipmentSlot> getActiveSlots() {
        return this.paperarc$delegate().getActiveSlots();
    }
}
