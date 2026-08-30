package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.UnsafeValues} (generated).
 * Adds 26 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.UnsafeValues", remap = false)
public interface UnsafeValuesIfaceMixin {

    @Unique
    public abstract boolean hasDefaultEntityAttributes(org.bukkit.NamespacedKey p0);

    @Unique
    public abstract boolean isCollidable(org.bukkit.Material p0);

    @Unique
    public abstract boolean isSupportedApiVersion(java.lang.String p0);

    @Unique
    public abstract boolean isValidRepairItemStack(org.bukkit.inventory.ItemStack p0, org.bukkit.inventory.ItemStack p1);

    @Unique
    public abstract byte[] serializeEntity(org.bukkit.entity.Entity p0);

    @Unique
    public abstract byte[] serializeItem(org.bukkit.inventory.ItemStack p0);

    @Unique
    public abstract com.google.common.collect.Multimap<org.bukkit.attribute.Attribute, org.bukkit.attribute.AttributeModifier> getItemAttributes(org.bukkit.Material p0, org.bukkit.inventory.EquipmentSlot p1);

    @Unique
    public abstract int getProtocolVersion();

    @Unique
    public abstract int nextEntityId();

    @Unique
    public abstract io.papermc.paper.inventory.ItemRarity getItemRarity(org.bukkit.Material p0);

    @Unique
    public abstract io.papermc.paper.inventory.ItemRarity getItemStackRarity(org.bukkit.inventory.ItemStack p0);

    @Unique
    public abstract java.lang.String getMainLevelName();

    @Unique
    public abstract java.lang.String getStatisticCriteriaKey(org.bukkit.Statistic p0);

    @Unique
    public abstract java.lang.String getTimingsServerName();

    @Unique
    public abstract net.kyori.adventure.text.flattener.ComponentFlattener componentFlattener();

    @Unique
    public abstract net.kyori.adventure.text.serializer.gson.GsonComponentSerializer colorDownsamplingGsonComponentSerializer();

    @Unique
    public abstract net.kyori.adventure.text.serializer.gson.GsonComponentSerializer gsonComponentSerializer();

    @Unique
    public abstract net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer legacyComponentSerializer();

    @Unique
    public abstract net.kyori.adventure.text.serializer.plain.PlainComponentSerializer plainComponentSerializer();

    @Unique
    public abstract net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer plainTextSerializer();

    @Unique
    public abstract org.bukkit.NamespacedKey getBiomeKey(org.bukkit.RegionAccessor p0, int p1, int p2, int p3);

    @Unique
    public abstract org.bukkit.attribute.Attributable getDefaultEntityAttributes(org.bukkit.NamespacedKey p0);

    @Unique
    public abstract org.bukkit.entity.Entity deserializeEntity(byte[] p0, org.bukkit.World p1, boolean p2);

    @Unique
    public abstract org.bukkit.inventory.ItemStack deserializeItem(byte[] p0);

    @Unique
    public abstract void reportTimings();

    @Unique
    public abstract void setBiomeKey(org.bukkit.RegionAccessor p0, int p1, int p2, int p3, org.bukkit.NamespacedKey p4);

}