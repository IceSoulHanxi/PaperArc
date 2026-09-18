package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.attribute.Attribute;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

/**
 * 同 {@link EnchantmentWrapperApiMixin}：{@code PotionEffectTypeWrapper} 是另一族实现，
 * paper 在它上面把方法全部转发给 {@code getType()}（A6/X-2 第三批）。
 */
@Mixin(PotionEffectTypeWrapper.class)
public abstract class PotionEffectTypeWrapperApiMixin {

    @Unique
    private PotionEffectType paperarc$delegate() {
        return ((PotionEffectTypeWrapper) (Object) this).getType();
    }

    @Unique
    public String translationKey() {
        return this.paperarc$delegate().translationKey();
    }

    @Unique
    public Map<Attribute, org.bukkit.attribute.AttributeModifier> getEffectAttributes() {
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
}
