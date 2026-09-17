package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.inventory.CraftItemType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/** B2-6：{@code ItemType extends Translatable} 的终端方法。 */
@Mixin(CraftItemType.class)
public abstract class CraftItemTypeApiMixin {

    @Shadow
    public abstract net.minecraft.world.item.Item getHandle();

    @Unique
    public String translationKey() {
        return this.getHandle().getDescriptionId();
    }

    /**
     * B2-6 顺带补：{@code ItemTypeIfaceMixin} 从 B4 起就声明了下面两个方法，
     * 但一直没有实现体。1.21.1 里这两样都在物品的默认数据组件上。
     */
    @Unique
    public com.google.common.collect.Multimap getDefaultAttributeModifiers() {
        com.google.common.collect.ImmutableMultimap.Builder<org.bukkit.attribute.Attribute, org.bukkit.attribute.AttributeModifier> out = com.google.common.collect.ImmutableMultimap.builder();
        net.minecraft.world.item.component.ItemAttributeModifiers modifiers =
                this.getHandle().components().get(
                        net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers != null) {
            for (net.minecraft.world.item.component.ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
                org.bukkit.attribute.Attribute attribute =
                        org.bukkit.craftbukkit.v.attribute.CraftAttribute.minecraftHolderToBukkit(entry.attribute());
                if (attribute != null) {
                    out.put(attribute, org.bukkit.craftbukkit.v.attribute.CraftAttributeInstance
                            .convert(entry.modifier()));
                }
            }
        }
        return out.build();
    }

    @Unique
    public org.bukkit.inventory.ItemRarity getItemRarity() {
        net.minecraft.world.item.Rarity rarity = this.getHandle().components().get(
                net.minecraft.core.component.DataComponents.RARITY);
        if (rarity == null) {
            return org.bukkit.inventory.ItemRarity.COMMON;
        }
        return switch (rarity) {
            case UNCOMMON -> org.bukkit.inventory.ItemRarity.UNCOMMON;
            case RARE -> org.bukkit.inventory.ItemRarity.RARE;
            case EPIC -> org.bukkit.inventory.ItemRarity.EPIC;
            default -> org.bukkit.inventory.ItemRarity.COMMON;
        };
    }
}
