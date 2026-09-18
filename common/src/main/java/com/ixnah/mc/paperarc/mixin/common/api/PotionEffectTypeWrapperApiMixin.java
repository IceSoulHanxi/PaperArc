package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

/**
 * paper 加在 {@code PotionEffectTypeWrapper} 上的 7 个方法（checklist §1.10 am，第 ⑤ 批）：
 * 全部转调被包装的 {@code getType()}，那一侧由 {@code bukkit.PotionEffectTypeIfaceMixin}
 * + {@code api.CraftPotionEffectTypeApiMixin} 提供。
 */
@Mixin(PotionEffectTypeWrapper.class)
public abstract class PotionEffectTypeWrapperApiMixin {

    @Unique
    private PotionEffectType paperarc$delegate() {
        return ((PotionEffectTypeWrapper) (Object) this).getType();
    }

    @Unique
    public boolean isInstant() {
        return this.paperarc$delegate().isInstant();
    }

    @Unique
    public Color getColor() {
        return this.paperarc$delegate().getColor();
    }

    @Unique
    public NamespacedKey getKey() {
        return this.paperarc$delegate().getKey();
    }

    @Unique
    public Map<Attribute, AttributeModifier> getEffectAttributes() {
        return this.paperarc$delegate().getEffectAttributes();
    }

    @Unique
    public double getAttributeModifierAmount(Attribute attribute, int effectAmplifier) {
        return this.paperarc$delegate().getAttributeModifierAmount(attribute, effectAmplifier);
    }

    @Unique
    public PotionEffectType.Category getEffectCategory() {
        return this.paperarc$delegate().getEffectCategory();
    }

    @Unique
    public String translationKey() {
        return this.paperarc$delegate().translationKey();
    }
}
