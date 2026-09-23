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
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;

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
                BuiltInRegistries.ENTITY_TYPE.getValue(CraftNamespacedKey.toMinecraft(bukkitEntityKey)));
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
        return CraftItemStack.asNMSCopy(itemToBeRepaired).isValidRepairItem(
                CraftItemStack.asNMSCopy(repairMaterial));
    }

    @Unique
    public byte[] serializeItem(ItemStack item) {
        Preconditions.checkNotNull(item, "null cannot be serialized");
        Preconditions.checkArgument(item.getType() != org.bukkit.Material.AIR, "air cannot be serialized");
        net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(item);
        TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, paperarc$registries());
        out.store("Item", net.minecraft.world.item.ItemStack.CODEC, nms);
        return serializeNbtToBytes(out.buildResult());
    }

    @Unique
    public ItemStack deserializeItem(byte[] data) {
        Preconditions.checkNotNull(data, "null cannot be deserialized");
        Preconditions.checkArgument(data.length > 0, "cannot deserialize nothing");
        CompoundTag compound = deserializeNbtFromBytes(data);
        ValueInput in = TagValueInput.create(ProblemReporter.DISCARDING, paperarc$registries(), compound);
        return CraftItemStack.asCraftMirror(
                in.read("Item", net.minecraft.world.item.ItemStack.CODEC).orElse(net.minecraft.world.item.ItemStack.EMPTY));
    }

    @Unique
    public byte[] serializeEntity(Entity entity) {
        Preconditions.checkNotNull(entity, "null cannot be serialized");
        Preconditions.checkArgument(entity instanceof CraftEntity, "only CraftEntities can be serialized");
        net.minecraft.world.entity.Entity nms = ((CraftEntity) entity).getHandle();
        TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, nms.registryAccess());
        nms.saveWithoutId(out);
        return serializeNbtToBytes(out.buildResult());
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
        net.minecraft.server.level.ServerLevel serverLevel = ((org.bukkit.craftbukkit.v.CraftWorld) world).getHandle();
        ValueInput in = TagValueInput.create(ProblemReporter.DISCARDING, serverLevel.registryAccess(), compound);
        return PaperArcBridge.bukkitEntity(
                net.minecraft.world.entity.EntityType.create(in, serverLevel, net.minecraft.world.entity.EntitySpawnReason.LOAD)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "An ID was not found for the data. Did you downgrade?")));
    }

    @Unique
    public int getProtocolVersion() {
        return net.minecraft.SharedConstants.getCurrentVersion().protocolVersion();
    }

    @Unique
    public int nextEntityId() {
        return net.minecraft.world.entity.Entity.ENTITY_COUNTER.incrementAndGet();
    }

    @Unique
    public String getMainLevelName() {
        net.minecraft.server.MinecraftServer server = paperarc$server();
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
    public NamespacedKey getBiomeKey(org.bukkit.RegionAccessor accessor, int x, int y, int z) {
        CraftRegionAccessor cra = (CraftRegionAccessor) accessor;
        return CraftNamespacedKey.fromMinecraft(cra.getHandle().registryAccess()
                .lookupOrThrow(Registries.BIOME)
                .getKey(cra.getHandle().getBiome(new BlockPos(x, y, z)).value()));
    }

    @Unique
    public void setBiomeKey(org.bukkit.RegionAccessor accessor, int x, int y, int z, NamespacedKey biomeKey) {
        CraftRegionAccessor cra = (CraftRegionAccessor) accessor;
        net.minecraft.core.Holder<Biome> biomeBase = cra.getHandle().registryAccess()
                .lookupOrThrow(Registries.BIOME)
                .getOrThrow(ResourceKey.create(Registries.BIOME,
                        CraftNamespacedKey.toMinecraft(biomeKey)));
        cra.setBiome(x, y, z, biomeBase);
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
            // 1.21.1 的 readCompressed 需要 NbtAccounter 限额；用 unlimitedHeap 保持旧行为
            CompoundTag compound = NbtIo.readCompressed(new ByteArrayInputStream(data),
                    net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            int dataVersion = compound.getIntOr("DataVersion", -1);
            Preconditions.checkArgument(dataVersion <= CraftMagicNumbers.INSTANCE.getDataVersion(),
                    "Newer version! Server downgrades are not supported!");
            return compound;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Unique
    private static net.minecraft.server.MinecraftServer paperarc$server() {
        try {
            return ((org.bukkit.craftbukkit.v.CraftServer) PaperArcBridge.getServer()).getServer();
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** 1.21.1 的 NBT/数据组件序列化都要注册表视图。 */
    @Unique
    private net.minecraft.core.HolderLookup.Provider paperarc$registries() {
        return ((org.bukkit.craftbukkit.v.CraftServer) org.bukkit.Bukkit.getServer())
                .getServer().registryAccess();
    }

    // ---------------------------------------------------------------- B3-4：最后 10 条抽象缺口

    /** paper {@code UnsafeValues#createEmptyStack}：一个空 ItemStack。 */
    @Unique
    public org.bukkit.inventory.ItemStack createEmptyStack() {
        return org.bukkit.craftbukkit.v.inventory.CraftItemStack.asCraftMirror(
                net.minecraft.world.item.ItemStack.EMPTY);
    }

    /**
     * paper {@code UnsafeValues#getSpawnEggLayerColor}：1.21.4 起刷怪蛋颜色改由客户端物品模型着色，
     * 服务端（{@code SpawnEggItem}）不再有颜色数据；paper-api 同版本起把它标为 {@code @Deprecated}
     * 且返回 {@code @Nullable}，这里返回 {@code null} 就是该语义下唯一可给出的结果。
     */
    @Unique
    public org.bukkit.Color getSpawnEggLayerColor(org.bukkit.entity.EntityType entityType, int layer) {
        return null;
    }

    /** paper {@code UnsafeValues#serializeItemAsJson}：走 vanilla 的 ItemStack CODEC。 */
    @Unique
    public com.google.gson.JsonObject serializeItemAsJson(org.bukkit.inventory.ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nms =
                org.bukkit.craftbukkit.v.inventory.CraftItemStack.asNMSCopy(itemStack);
        com.mojang.serialization.DynamicOps<com.google.gson.JsonElement> ops =
                paperarc$registries().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
        return net.minecraft.world.item.ItemStack.CODEC.encodeStart(ops, nms)
                .getOrThrow(IllegalArgumentException::new).getAsJsonObject();
    }

    /** paper {@code UnsafeValues#deserializeItemFromJson}：{@link #serializeItemAsJson} 的逆。 */
    @Unique
    public org.bukkit.inventory.ItemStack deserializeItemFromJson(com.google.gson.JsonObject data) {
        com.mojang.serialization.DynamicOps<com.google.gson.JsonElement> ops =
                paperarc$registries().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
        net.minecraft.world.item.ItemStack nms = net.minecraft.world.item.ItemStack.CODEC
                .parse(ops, data).getOrThrow(IllegalArgumentException::new);
        return org.bukkit.craftbukkit.v.inventory.CraftItemStack.asCraftMirror(nms);
    }

    /**
     * paper {@code UnsafeValues#computeTooltipLines}：转调 vanilla 的
     * {@code ItemStack#getTooltipLines}。
     *
     * <p>偏差：paper 的 {@code TooltipContext} 还带 {@code isCreative()}/{@code isAdvanced()}
     * 之外的上下文，这里只映射到 vanilla 的 {@code TooltipFlag}；{@code player} 为 null 时
     * 按"无玩家"渲染（vanilla 允许）。
     */
    @Unique
    public java.util.List<net.kyori.adventure.text.Component> computeTooltipLines(
            org.bukkit.inventory.ItemStack itemStack,
            io.papermc.paper.inventory.tooltip.TooltipContext tooltipContext,
            org.bukkit.entity.Player player) {
        net.minecraft.world.item.ItemStack nms =
                org.bukkit.craftbukkit.v.inventory.CraftItemStack.asNMSCopy(itemStack);
        net.minecraft.world.item.TooltipFlag.Default flag =
                tooltipContext != null && tooltipContext.isAdvanced()
                        ? net.minecraft.world.item.TooltipFlag.ADVANCED
                        : net.minecraft.world.item.TooltipFlag.NORMAL;
        if (tooltipContext != null && tooltipContext.isCreative()) {
            flag = flag.asCreative();
        }
        net.minecraft.server.level.ServerPlayer handle = player == null ? null
                : ((org.bukkit.craftbukkit.v.entity.CraftPlayer) player).getHandle();
        net.minecraft.world.item.Item.TooltipContext ctx =
                net.minecraft.world.item.Item.TooltipContext.of(paperarc$registries());
        java.util.List<net.kyori.adventure.text.Component> out = new java.util.ArrayList<>();
        for (net.minecraft.network.chat.Component line : nms.getTooltipLines(ctx, handle, flag)) {
            out.add(net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson()
                    .deserialize(org.bukkit.craftbukkit.v.util.CraftChatMessage.ChatSerializer
                            .toJson(line, paperarc$registries())));
        }
        return out;
    }

    /**
     * paper {@code UnsafeValues#resolveWithContext}：把组件里的选择器/记分板占位解析成实际内容。
     *
     * <p>照 Paper 走 vanilla 的 {@code ComponentUtils#updateForEntity}。
     * 反向映射 {@code CommandSender → CommandSourceStack} 走 CraftBukkit 自带的
     * {@code VanillaCommandWrapper#getListener}（B7/Y-4 之前这里是原样返回的占位实现）。
     * {@code bypassPermissions} 按 Paper 提到权限级 2（选择器需要的级别）。
     */
    @Unique
    public net.kyori.adventure.text.Component resolveWithContext(
            net.kyori.adventure.text.Component component, org.bukkit.command.CommandSender context,
            org.bukkit.entity.Entity scoreboardSubject, boolean bypassPermissions) {
        return com.ixnah.mc.paperarc.bridge.api.PaperarcComponents.resolveWithContext(
                component, context, scoreboardSubject, bypassPermissions);
    }

    /**
     * paper {@code UnsafeValues#createPluginLifecycleEventManager}：插件生命周期事件管理器。
     *
     * <p><b>占位实现</b>：抛 {@code UnsupportedOperationException}。这套 API 要求 paper 的
     * 插件引导（{@code PluginBootstrap}）与 Brigadier 命令注册管线，Arclight 的插件加载器
     * 两样都没有；返回一个空管理器会让插件以为注册成功、实际回调永不触发，比直接报错更坏。
     * 记 `docs/gaps.md`。
     */
    @Unique
    public <T extends io.papermc.paper.plugin.lifecycle.event.registrar.RegistrarEvent>
            io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager<org.bukkit.plugin.Plugin>
            createPluginLifecycleEventManager(org.bukkit.plugin.java.JavaPlugin plugin,
                    java.util.function.BooleanSupplier registrationCheck) {
        throw new UnsupportedOperationException(
                "PaperArc: Arclight 没有 paper 的插件生命周期/Brigadier 管线，"
                        + "getLifecycleManager() 不可用（docs/gaps.md）");
    }
}
