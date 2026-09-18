package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@link org.bukkit.potion.PotionEffectType} 是**抽象类**不是接口，用类目标 mixin 加
 * {@code @Unique public abstract} 声明；实现体在 {@code api.CraftPotionEffectTypeApiMixin}
 * （唯一的运行时子类 CraftPotionEffectType）。
 */
@Mixin(targets = "org.bukkit.potion.PotionEffectType", remap = false)
public abstract class PotionEffectTypeIfaceMixin {

    @Unique
    public abstract java.util.Map<org.bukkit.attribute.Attribute, org.bukkit.attribute.AttributeModifier> getEffectAttributes();

    @Unique
    public abstract double getAttributeModifierAmount(org.bukkit.attribute.Attribute p0, int p1);

    @Unique
    public abstract org.bukkit.potion.PotionEffectType.Category getEffectCategory();

    /** paper 的 Translatable 抽象方法名（运行时 spigot-api 叫 getTranslationKey）。 */
    @Unique
    public abstract java.lang.String translationKey();
}
