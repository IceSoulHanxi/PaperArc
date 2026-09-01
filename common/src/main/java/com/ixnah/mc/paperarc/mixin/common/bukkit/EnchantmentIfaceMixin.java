package com.ixnah.mc.paperarc.mixin.common.bukkit;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v.enchantments.CraftEnchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Class augmentation for {@link org.bukkit.enchantments.Enchantment} (generated).
 * <p>Unlike the interface targets, {@code org.bukkit.enchantments.Enchantment} is an
 * <em>abstract class</em>, so methods must be injected with concrete bodies here
 * (invokevirtual on the static base type requires the method to exist on the class
 * itself). All bodies delegate to the NMS enchantment via {@link CraftEnchantment#getRaw}.</p>
 */
@Mixin(targets = "org.bukkit.enchantments.Enchantment", remap = false)
public abstract class EnchantmentIfaceMixin {

    @Unique
    public boolean isDiscoverable() {
        return CraftEnchantment.getRaw((org.bukkit.enchantments.Enchantment) (Object) this).isDiscoverable();
    }

    @Unique
    public boolean isTradeable() {
        return CraftEnchantment.getRaw((org.bukkit.enchantments.Enchantment) (Object) this).isTradeable();
    }

    @Unique
    public float getDamageIncrease(int level, org.bukkit.entity.EntityCategory entityCategory) {
        return CraftEnchantment.getRaw((org.bukkit.enchantments.Enchantment) (Object) this)
                .getDamageBonus(level, fromBukkitEntityCategory(entityCategory));
    }

    @Unique
    public io.papermc.paper.enchantments.EnchantmentRarity getRarity() {
        return fromNMSRarity(CraftEnchantment.getRaw((org.bukkit.enchantments.Enchantment) (Object) this).getRarity());
    }

    @Unique
    public Set<org.bukkit.inventory.EquipmentSlot> getActiveSlots() {
        net.minecraft.world.item.enchantment.Enchantment nms =
                CraftEnchantment.getRaw((org.bukkit.enchantments.Enchantment) (Object) this);
        return Stream.of(nms.slots)
                .map(CraftEquipmentSlot::getSlot)
                .collect(Collectors.toSet());
    }

    @Unique
    public Component displayName(int level) {
        net.minecraft.network.chat.Component nms =
                CraftEnchantment.getRaw((org.bukkit.enchantments.Enchantment) (Object) this).getFullname(level);
        return GsonComponentSerializer.gson().deserialize(
                net.minecraft.network.chat.Component.Serializer.toJson(nms));
    }

    @Unique
    private static io.papermc.paper.enchantments.EnchantmentRarity fromNMSRarity(
            net.minecraft.world.item.enchantment.Enchantment.Rarity rarity) {
        switch (rarity) {
            case COMMON: return io.papermc.paper.enchantments.EnchantmentRarity.COMMON;
            case UNCOMMON: return io.papermc.paper.enchantments.EnchantmentRarity.UNCOMMON;
            case RARE: return io.papermc.paper.enchantments.EnchantmentRarity.RARE;
            default: return io.papermc.paper.enchantments.EnchantmentRarity.VERY_RARE;
        }
    }

    @Unique
    private static net.minecraft.world.entity.MobType fromBukkitEntityCategory(
            org.bukkit.entity.EntityCategory category) {
        switch (category) {
            case UNDEAD: return net.minecraft.world.entity.MobType.UNDEAD;
            case ARTHROPOD: return net.minecraft.world.entity.MobType.ARTHROPOD;
            case ILLAGER: return net.minecraft.world.entity.MobType.ILLAGER;
            case WATER: return net.minecraft.world.entity.MobType.WATER;
            default: return net.minecraft.world.entity.MobType.UNDEFINED;
        }
    }
}
