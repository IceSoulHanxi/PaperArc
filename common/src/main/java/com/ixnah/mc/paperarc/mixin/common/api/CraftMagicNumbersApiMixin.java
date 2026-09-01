package com.ixnah.mc.paperarc.mixin.common.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v.CraftRegionAccessor;
import org.bukkit.craftbukkit.v.CraftStatistic;
import org.bukkit.craftbukkit.v.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v.attribute.CraftAttributeInstance;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v.util.CraftNamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's {@code UnsafeValues} methods to {@link CraftMagicNumbers} (the
 * {@code Bukkit.getUnsafe()} implementation).
 *
 * <p>Most methods mirror the Paper patches 1:1. Methods that depend on
 * Paper-private infrastructure absent from Arclight are degraded:</p>
 * <ul>
 *   <li>serializer accessors ({@code componentFlattener/gsonComponentSerializer/
 *       legacyComponentSerializer/plainComponentSerializer/plainTextSerializer/
 *       colorDownsamplingGsonComponentSerializer}) return plain adventure defaults
 *       instead of PaperAdventure-wired instances.</li>
 *   <li>{@code serializeItem/deserializeItem/serializeEntity/deserializeEntity}
 *       skip the {@code MCDataConverter} (data-fixer) step (no dataconverter
 *       module in Arclight); raw NBT round-trip is preserved.</li>
 *   <li>{@code getDefaultEntityAttributes} returns {@code null} (no Paper
 *       {@code UnmodifiableAttributeMap}).</li>
 *   <li>{@code getTimingsServerName/reportTimings} degrade to server motd / no-op.</li>
 * </ul>
 */
@Mixin(CraftMagicNumbers.class)
public abstract class CraftMagicNumbersApiMixin {

    // -------------------------------------------------------------------
    // implemented 1:1 with Paper patches
    // -------------------------------------------------------------------

    @Unique
    public boolean hasDefaultEntityAttributes(NamespacedKey bukkitEntityKey) {
        return net.minecraft.world.entity.ai.attributes.DefaultAttributes.hasSupplier(
                BuiltInRegistries.ENTITY_TYPE.get(CraftNamespacedKey.toMinecraft(bukkitEntityKey)));
    }

    @Unique
    public boolean isCollidable(org.bukkit.Material material) {
        Preconditions.checkArgument(material.isBlock(), material + " is not a block");
        return CraftMagicNumbers.getBlock(material).hasCollision;
    }

    @Unique
    public boolean isSupportedApiVersion(String apiVersion) {
        // Paper keeps a SUPPORTED_API set; Arclight has none, so accept any 1.x >= 1.13.
        if (apiVersion == null || !apiVersion.startsWith("1.")) {
            return false;
        }
        try {
            return Double.parseDouble(apiVersion.substring(1)) >= 1.13;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    @Unique
    public boolean isValidRepairItemStack(ItemStack itemToBeRepaired, ItemStack repairMaterial) {
        if (!itemToBeRepaired.getType().isItem() || !repairMaterial.getType().isItem()) {
            return false;
        }
        return CraftMagicNumbers.getItem(itemToBeRepaired.getType())
                .isValidRepairItem(CraftItemStack.asNMSCopy(itemToBeRepaired),
                        CraftItemStack.asNMSCopy(repairMaterial));
    }

    @Unique
    public byte[] serializeItem(ItemStack item) {
        Preconditions.checkNotNull(item, "null cannot be serialized");
        Preconditions.checkArgument(item.getType() != org.bukkit.Material.AIR, "air cannot be serialized");
        net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(item);
        return serializeNbtToBytes(nms.save(new CompoundTag()));
    }

    @Unique
    public ItemStack deserializeItem(byte[] data) {
        Preconditions.checkNotNull(data, "null cannot be deserialized");
        Preconditions.checkArgument(data.length > 0, "cannot deserialize nothing");
        return CraftItemStack.asCraftMirror(
                net.minecraft.world.item.ItemStack.of(deserializeNbtFromBytes(data)));
    }

    @Unique
    public byte[] serializeEntity(Entity entity) {
        Preconditions.checkNotNull(entity, "null cannot be serialized");
        Preconditions.checkArgument(entity instanceof CraftEntity, "only CraftEntities can be serialized");
        CompoundTag compound = new CompoundTag();
        ((CraftEntity) entity).getHandle().saveWithoutId(compound);
        return serializeNbtToBytes(compound);
    }

    @Unique
    public Entity deserializeEntity(byte[] data, org.bukkit.World world, boolean preserveUUID) {
        Preconditions.checkNotNull(data, "null cannot be deserialized");
        Preconditions.checkArgument(data.length > 0, "cannot deserialize nothing");
        CompoundTag compound = deserializeNbtFromBytes(data);
        // NOTE: MCDataConverter (data-fixer) skipped; Arclight has no dataconverter module.
        if (!preserveUUID) {
            compound.remove("UUID");
        }
        return PaperArcBridge.bukkitEntity(
                net.minecraft.world.entity.EntityType.create(compound,
                                ((org.bukkit.craftbukkit.v.CraftWorld) world).getHandle())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "An ID was not found for the data. Did you downgrade?")));
    }

    @Unique
    public Multimap<Attribute, AttributeModifier> getItemAttributes(
            org.bukkit.Material material, org.bukkit.inventory.EquipmentSlot equipmentSlot) {
        Item item = CraftMagicNumbers.getItem(material);
        if (item == null) {
            throw new IllegalArgumentException(material + " is not an item and therefore does not have attributes");
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        item.getDefaultAttributeModifiers(CraftEquipmentSlot.getNMS(equipmentSlot))
                .forEach((attributeBase, attributeModifier) ->
                        builder.put(
                                CraftAttributeMap.fromMinecraft(
                                        BuiltInRegistries.ATTRIBUTE.getKey(attributeBase).toString()),
                                CraftAttributeInstance.convert(attributeModifier, equipmentSlot)));
        return builder.build();
    }

    @Unique
    public int getProtocolVersion() {
        return net.minecraft.SharedConstants.getCurrentVersion().getProtocolVersion();
    }

    @Unique
    public int nextEntityId() {
        return net.minecraft.world.entity.Entity.ENTITY_COUNTER.incrementAndGet();
    }

    @Unique
    public io.papermc.paper.inventory.ItemRarity getItemRarity(org.bukkit.Material material) {
        Item item = CraftMagicNumbers.getItem(material);
        if (item == null) {
            throw new IllegalArgumentException(material + " is not an item, and rarity does not apply to blocks");
        }
        return io.papermc.paper.inventory.ItemRarity.values()[item.rarity.ordinal()];
    }

    @Unique
    public io.papermc.paper.inventory.ItemRarity getItemStackRarity(ItemStack itemStack) {
        return io.papermc.paper.inventory.ItemRarity.values()[
                CraftMagicNumbers.getItem(itemStack.getType())
                        .getRarity(CraftItemStack.asNMSCopy(itemStack)).ordinal()];
    }

    @Unique
    public String getMainLevelName() {
        net.minecraft.server.MinecraftServer server = server();
        if (server instanceof DedicatedServer dedicatedServer) {
            return dedicatedServer.getProperties().levelName;
        }
        return "";
    }

    @Unique
    public String getStatisticCriteriaKey(Statistic statistic) {
        if (statistic.getType() != Statistic.Type.UNTYPED) {
            return "minecraft.custom:minecraft." + statistic.getKey().getKey();
        }
        return CraftStatistic.getNMSStatistic(statistic).getName();
    }

    @Unique
    public String getTimingsServerName() {
        net.minecraft.server.MinecraftServer server = server();
        return server != null ? server.getMotd() : "PaperArc";
    }

    @Unique
    public NamespacedKey getBiomeKey(org.bukkit.RegionAccessor accessor, int x, int y, int z) {
        CraftRegionAccessor cra = (CraftRegionAccessor) accessor;
        return CraftNamespacedKey.fromMinecraft(cra.getHandle().registryAccess()
                .registryOrThrow(Registries.BIOME)
                .getKey(cra.getHandle().getBiome(new BlockPos(x, y, z)).value()));
    }

    @Unique
    public void setBiomeKey(org.bukkit.RegionAccessor accessor, int x, int y, int z, NamespacedKey biomeKey) {
        CraftRegionAccessor cra = (CraftRegionAccessor) accessor;
        net.minecraft.core.Holder<Biome> biomeBase = cra.getHandle().registryAccess()
                .registryOrThrow(Registries.BIOME)
                .getHolderOrThrow(ResourceKey.create(Registries.BIOME,
                        CraftNamespacedKey.toMinecraft(biomeKey)));
        cra.setBiome(x, y, z, biomeBase);
    }

    @Unique
    public void reportTimings() {
        // Arclight has no Paper Timings; no-op.
    }

    // -------------------------------------------------------------------
    // degraded (Paper-private dependencies absent in Arclight)
    // -------------------------------------------------------------------

    @Unique
    public ComponentFlattener componentFlattener() {
        return ComponentFlattener.basic();
    }

    @Unique
    public GsonComponentSerializer colorDownsamplingGsonComponentSerializer() {
        return GsonComponentSerializer.colorDownsamplingGson();
    }

    @Unique
    public GsonComponentSerializer gsonComponentSerializer() {
        return GsonComponentSerializer.gson();
    }

    @Unique
    public LegacyComponentSerializer legacyComponentSerializer() {
        return LegacyComponentSerializer.legacySection();
    }

    @Unique
    public PlainComponentSerializer plainComponentSerializer() {
        return PlainComponentSerializer.plain();
    }

    @Unique
    public PlainTextComponentSerializer plainTextSerializer() {
        return PlainTextComponentSerializer.plainText();
    }

    @Unique
    public org.bukkit.attribute.Attributable getDefaultEntityAttributes(NamespacedKey bukkitEntityKey) {
        return null; // no Paper UnmodifiableAttributeMap in Arclight
    }

    // -------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------

    @Unique
    private byte[] serializeNbtToBytes(CompoundTag compound) {
        compound.putInt("DataVersion", CraftMagicNumbers.INSTANCE.getDataVersion());
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            NbtIo.writeCompressed(compound, out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Unique
    private CompoundTag deserializeNbtFromBytes(byte[] data) {
        try {
            CompoundTag compound = NbtIo.readCompressed(new ByteArrayInputStream(data));
            int dataVersion = compound.getInt("DataVersion");
            Preconditions.checkArgument(dataVersion <= CraftMagicNumbers.INSTANCE.getDataVersion(),
                    "Newer version! Server downgrades are not supported!");
            return compound;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Unique
    private static net.minecraft.server.MinecraftServer server() {
        try {
            return ((org.bukkit.craftbukkit.v.CraftServer) PaperArcBridge.getServer()).getServer();
        } catch (Throwable ignored) {
            return null;
        }
    }
}
