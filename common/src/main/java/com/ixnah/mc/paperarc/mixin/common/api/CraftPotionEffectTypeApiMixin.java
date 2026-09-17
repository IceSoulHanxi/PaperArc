package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.effect.MobEffect;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v.potion.CraftPotionEffectType;
import org.bukkit.potion.PotionEffectType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper 的 {@code PotionEffectType#getEffectAttributes()/getAttributeModifierAmount(..)/getEffectCategory()}。
 *
 * <p>NMS {@code MobEffect} 用 {@code createModifiers(amplifier, BiConsumer)} 产出这一级的修饰符，
 * 这里按 amplifier 0 收集属性表、按传入 amplifier 取具体数值。</p>
 */
@Mixin(CraftPotionEffectType.class)
public abstract class CraftPotionEffectTypeApiMixin {

    @Shadow
    public abstract MobEffect getHandle();

    @Unique
    public java.util.Map<Attribute, AttributeModifier> getEffectAttributes() {
        java.util.Map<Attribute, AttributeModifier> out = new java.util.HashMap<>();
        this.getHandle().createModifiers(0, (holder, modifier) -> {
            Attribute bukkit = org.bukkit.craftbukkit.v.attribute.CraftAttribute.minecraftHolderToBukkit(holder);
            if (bukkit != null) {
                out.put(bukkit, org.bukkit.craftbukkit.v.attribute.CraftAttributeInstance.convert(modifier));
            }
        });
        return java.util.Collections.unmodifiableMap(out);
    }

    @Unique
    public double getAttributeModifierAmount(Attribute attribute, int effectAmplifier) {
        com.google.common.base.Preconditions.checkArgument(attribute != null, "attribute cannot be null");
        com.google.common.base.Preconditions.checkArgument(effectAmplifier >= 0,
                "effectAmplifier must be greater than or equal to 0");
        double[] amount = new double[]{0.0D};
        this.getHandle().createModifiers(effectAmplifier, (holder, modifier) -> {
            if (org.bukkit.craftbukkit.v.attribute.CraftAttribute.minecraftHolderToBukkit(holder) == attribute) {
                amount[0] = modifier.amount();
            }
        });
        return amount[0];
    }

    @Unique
    public PotionEffectType.Category getEffectCategory() {
        return switch (this.getHandle().getCategory()) {
            case BENEFICIAL -> PotionEffectType.Category.BENEFICIAL;
            case HARMFUL -> PotionEffectType.Category.HARMFUL;
            default -> PotionEffectType.Category.NEUTRAL;
        };
    }
}
