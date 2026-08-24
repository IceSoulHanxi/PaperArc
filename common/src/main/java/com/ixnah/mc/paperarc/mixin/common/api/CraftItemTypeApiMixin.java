package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Locale;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v.attribute.CraftAttribute;
import org.bukkit.craftbukkit.v.inventory.CraftItemType;
import org.bukkit.craftbukkit.v.util.CraftNamespacedKey;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemRarity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's ItemType default-attribute and rarity API.
 *
 * Paper builds the multimap from DataComponents.ATTRIBUTE_MODIFIERS (falling
 * back to Item#getDefaultAttributeModifiers) and converts each entry with a
 * CraftAttributeInstance.convert overload that takes an EquipmentSlotGroup;
 * Arclight's CB lacks both that overload and CraftEquipmentSlotGroup, so the
 * modifier conversion (id/amount/operation/slot-group) is inlined here.
 */
@Mixin(CraftItemType.class)
public abstract class CraftItemTypeApiMixin {

    @Shadow
    @Final
    private net.minecraft.world.item.Item item;

    @Unique
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers() {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> defaultAttributes = ImmutableMultimap.builder();

        ItemAttributeModifiers nmsDefaultAttributes =
                item.components().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        if (nmsDefaultAttributes.modifiers().isEmpty()) {
            nmsDefaultAttributes = item.getDefaultAttributeModifiers();
        }

        // Paper predicate is sg -> true (no-arg overload): every entry passes.
        for (ItemAttributeModifiers.Entry entry : nmsDefaultAttributes.modifiers()) {
            Attribute attribute = CraftAttribute.minecraftHolderToBukkit(entry.attribute());
            AttributeModifier modifier = paperarc$convert(entry.modifier(), entry.slot());
            defaultAttributes.put(attribute, modifier);
        }

        return defaultAttributes.build();
    }

    @Unique
    private static AttributeModifier paperarc$convert(net.minecraft.world.entity.ai.attributes.AttributeModifier nms,
                                                      net.minecraft.world.entity.EquipmentSlotGroup slotGroup) {
        NamespacedKey key = CraftNamespacedKey.fromMinecraft(nms.id());
        AttributeModifier.Operation operation = AttributeModifier.Operation.values()[nms.operation().ordinal()];
        EquipmentSlotGroup group = EquipmentSlotGroup.getByName(slotGroup.getSerializedName());
        return new AttributeModifier(key, nms.amount(), operation, group);
    }

    @Unique
    public ItemRarity getItemRarity() {
        net.minecraft.world.item.Rarity rarity = item.components().get(DataComponents.RARITY);
        return rarity == null ? null : ItemRarity.valueOf(rarity.name());
    }
}
