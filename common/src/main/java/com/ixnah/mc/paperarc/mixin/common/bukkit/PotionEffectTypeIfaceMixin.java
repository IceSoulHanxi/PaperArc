package com.ixnah.mc.paperarc.mixin.common.bukkit;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v.attribute.CraftAttributeInstance;
import org.bukkit.craftbukkit.v.potion.CraftPotionEffectType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Class augmentation for {@link org.bukkit.potion.PotionEffectType} (generated).
 * <p>Like {@link EnchantmentIfaceMixin}, the target is an abstract class, so methods
 * carry concrete bodies here, delegating to the NMS {@code MobEffect} via
 * {@link CraftPotionEffectType#getHandle}.</p>
 */
@Mixin(targets = "org.bukkit.potion.PotionEffectType", remap = false)
public abstract class PotionEffectTypeIfaceMixin {

    @Unique
    public Map<Attribute, AttributeModifier> getEffectAttributes() {
        Map<Attribute, AttributeModifier> map = new HashMap<>();
        handle().getAttributeModifiers().forEach((attribute, modifier) ->
                map.put(CraftAttributeMap.fromMinecraft(
                                net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.getKey(attribute).toString()),
                        CraftAttributeInstance.convert(modifier)));
        return Map.copyOf(map);
    }

    @Unique
    public double getAttributeModifierAmount(Attribute attribute, int effectAmplifier) {
        Preconditions.checkArgument(effectAmplifier >= 0, "Cannot have negative amplifier");
        net.minecraft.world.effect.MobEffect handle = handle();
        net.minecraft.world.entity.ai.attributes.Attribute nmsAttribute =
                CraftAttributeMap.toMinecraft(attribute);
        Map<net.minecraft.world.entity.ai.attributes.Attribute,
                net.minecraft.world.entity.ai.attributes.AttributeModifier> map =
                handle.getAttributeModifiers();
        Preconditions.checkArgument(map.containsKey(nmsAttribute),
                "Attribute not present on the effect");
        return handle.getAttributeModifierValue(effectAmplifier, map.get(nmsAttribute));
    }

    @Unique
    public org.bukkit.potion.PotionEffectType.Category getEffectCategory() {
        return fromNMS(handle().getCategory());
    }

    @Unique
    private net.minecraft.world.effect.MobEffect handle() {
        // PotionEffectType.SPEED etc. are PotionEffectTypeWrapper instances at runtime;
        // unwrap via getType() before the CraftPotionEffectType cast.
        org.bukkit.potion.PotionEffectType type =
                (org.bukkit.potion.PotionEffectType) (Object) this;
        while (type instanceof org.bukkit.potion.PotionEffectTypeWrapper wrapper) {
            type = wrapper.getType();
        }
        return ((CraftPotionEffectType) type).getHandle();
    }

    @Unique
    private static org.bukkit.potion.PotionEffectType.Category fromNMS(
            net.minecraft.world.effect.MobEffectCategory category) {
        switch (category) {
            case BENEFICIAL: return org.bukkit.potion.PotionEffectType.Category.BENEFICIAL;
            case HARMFUL: return org.bukkit.potion.PotionEffectType.Category.HARMFUL;
            default: return org.bukkit.potion.PotionEffectType.Category.NEUTRAL;
        }
    }
}
