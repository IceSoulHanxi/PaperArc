package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.UnsafeValues;
import org.bukkit.World;
import com.destroystokyo.paper.util.VersionFetcher;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import io.papermc.paper.inventory.tooltip.TooltipContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.registry.tag.TagKey;
import java.io.IOException;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.advancement.Advancement;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.damage.DamageEffect;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CreativeCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.UnsafeValues}.
 * Adds paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).
 */
@Mixin(targets = "org.bukkit.UnsafeValues", remap = false)
public interface UnsafeValuesIfaceMixin {

    @Unique
    public abstract net.kyori.adventure.text.serializer.gson.GsonComponentSerializer colorDownsamplingGsonComponentSerializer();

    @Unique
    public abstract net.kyori.adventure.text.flattener.ComponentFlattener componentFlattener();

    @Unique
    public abstract org.bukkit.entity.Entity deserializeEntity(byte[] p0, org.bukkit.World p1, boolean p2);

    @Unique
    public abstract org.bukkit.inventory.ItemStack deserializeItem(byte[] p0);

    @Unique
    public abstract org.bukkit.NamespacedKey getBiomeKey(org.bukkit.RegionAccessor p0, int p1, int p2, int p3);

    @Unique
    public abstract org.bukkit.attribute.Attributable getDefaultEntityAttributes(org.bukkit.NamespacedKey p0);

    @Unique
    public abstract java.lang.String getMainLevelName();

    @Unique
    public abstract int getProtocolVersion();

    @Unique
    public abstract java.lang.String getStatisticCriteriaKey(org.bukkit.Statistic p0);

    @Unique
    public abstract java.lang.String getTimingsServerName();

    @Unique
    public abstract net.kyori.adventure.text.serializer.gson.GsonComponentSerializer gsonComponentSerializer();

    @Unique
    public abstract boolean hasDefaultEntityAttributes(org.bukkit.NamespacedKey p0);

    @Unique
    public abstract boolean isSupportedApiVersion(java.lang.String p0);

    @Unique
    public abstract boolean isValidRepairItemStack(org.bukkit.inventory.ItemStack p0, org.bukkit.inventory.ItemStack p1);

    @Unique
    public abstract net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer legacyComponentSerializer();

    @Unique
    public abstract int nextEntityId();

    @Unique
    public abstract net.kyori.adventure.text.serializer.plain.PlainComponentSerializer plainComponentSerializer();

    @Unique
    public abstract net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer plainTextSerializer();

    @Unique
    public abstract void reportTimings();

    @Unique
    public abstract byte[] serializeEntity(org.bukkit.entity.Entity p0);

    @Unique
    public abstract byte[] serializeItem(org.bukkit.inventory.ItemStack p0);

    @Unique
    public abstract void setBiomeKey(org.bukkit.RegionAccessor p0, int p1, int p2, int p3, org.bukkit.NamespacedKey p4);

    @Unique
    public default VersionFetcher getVersionFetcher() {
        return new VersionFetcher.DummyVersionFetcher();
    }

    @Unique
    public default Entity deserializeEntity(byte[] data, World world) {
        UnsafeValues self = (UnsafeValues) this;
        return self.deserializeEntity(data, world, false);
    }

    // B3-4：最后 8 条 UnsafeValues 抽象缺口的声明，实现体在 api/CraftMagicNumbersApiMixin。

    @Unique
    public abstract org.bukkit.inventory.ItemStack createEmptyStack();

    @Unique
    public abstract org.bukkit.Color getSpawnEggLayerColor(org.bukkit.entity.EntityType p0, int p1);

    @Unique
    public abstract com.google.gson.JsonObject serializeItemAsJson(org.bukkit.inventory.ItemStack p0);

    @Unique
    public abstract org.bukkit.inventory.ItemStack deserializeItemFromJson(com.google.gson.JsonObject p0);

    @Unique
    public abstract java.util.List<net.kyori.adventure.text.Component> computeTooltipLines(
            org.bukkit.inventory.ItemStack p0, io.papermc.paper.inventory.tooltip.TooltipContext p1,
            org.bukkit.entity.Player p2);

    @Unique
    public abstract net.kyori.adventure.text.Component resolveWithContext(
            net.kyori.adventure.text.Component p0, org.bukkit.command.CommandSender p1,
            org.bukkit.entity.Entity p2, boolean p3);

    @Unique
    public abstract <A extends org.bukkit.Keyed, M> io.papermc.paper.registry.tag.Tag<A> getTag(
            io.papermc.paper.registry.tag.TagKey<A> p0);

    @Unique
    public abstract <T extends io.papermc.paper.plugin.lifecycle.event.registrar.RegistrarEvent>
            io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager<org.bukkit.plugin.Plugin>
            createPluginLifecycleEventManager(org.bukkit.plugin.java.JavaPlugin p0,
                    java.util.function.BooleanSupplier p1);
}
