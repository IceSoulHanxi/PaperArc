package com.ixnah.mc.paperarc.mixin.common.api;

import io.papermc.paper.enchantments.EnchantmentRarity;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.EnchantmentTags;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v.enchantments.CraftEnchantment;
import org.bukkit.entity.EntityCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Paper 在 {@code org.bukkit.enchantments.Enchantment} 上加的 16 个方法。
 *
 * <p>1.21 之后附魔完全数据驱动（{@code Enchantment} 变成了 record，属性都在
 * {@code definition()} 里），所以大部分方法是直译。三处没有 NMS 对应、按下面的
 * 占位策略处理，与 Paper 的语义尽量贴近：</p>
 * <ul>
 *   <li>{@code getRarity()}：1.21 删掉了 Rarity 枚举，权重直接写在 definition 里。
 *       按 vanilla 1.20 的权重表反查（10/5/2/1 → COMMON/UNCOMMON/RARE/VERY_RARE），
 *       落不到区间的取最接近的一档。</li>
 *   <li>{@code isTradeable()} / {@code isDiscoverable()}：1.21 改成了附魔标签，
 *       分别对应 {@code #minecraft:tradeable} 与 {@code #minecraft:in_enchanting_table}。</li>
 *   <li>{@code getDamageIncrease(..)}：1.21 的额外伤害由 {@code damage} 效果组件按
 *       条件计算，不再有"按实体类别给固定加成"的模型。B7/Y-4 起照 Paper 硬编码 1.20 的公式
 *       （见 {@code bridge/api/PaperarcEnchantmentDamage}），{@code EntityType} 重载用
 *       vanilla 1.21.1 真正在用的 {@code #minecraft:sensitive_to_*} 标签判定。</li>
 * </ul>
 */
@Mixin(CraftEnchantment.class)
public abstract class CraftEnchantmentApiMixin {

    @Shadow
    public abstract net.minecraft.world.item.enchantment.Enchantment getHandle();

    @Unique
    private static Component paperarc$adventure(net.minecraft.network.chat.Component vanilla) {
        if (vanilla == null) {
            return null;
        }
        net.minecraft.core.RegistryAccess registries =
                ((org.bukkit.craftbukkit.v.CraftServer) org.bukkit.Bukkit.getServer())
                        .getServer().registryAccess();
        return GsonComponentSerializer.gson().deserialize(
                org.bukkit.craftbukkit.v.util.CraftChatMessage.ChatSerializer.toJson(vanilla, registries));
    }

    @Unique
    private static RegistryKeySet<ItemType> paperarc$items(HolderSet<net.minecraft.world.item.Item> set) {
        List<TypedKey<ItemType>> keys = new ArrayList<>();
        if (set != null) {
            for (Holder<net.minecraft.world.item.Item> holder : set) {
                holder.unwrapKey().ifPresent(rk -> keys.add(TypedKey.create(RegistryKey.ITEM,
                        net.kyori.adventure.key.Key.key(rk.identifier().getNamespace(),
                                rk.identifier().getPath()))));
            }
        }
        return RegistrySet.keySet(RegistryKey.ITEM, keys);
    }

    @Unique
    private boolean paperarc$hasTag(net.minecraft.tags.TagKey<net.minecraft.world.item.enchantment.Enchantment> tag) {
        net.minecraft.core.RegistryAccess registries =
                ((org.bukkit.craftbukkit.v.CraftServer) org.bukkit.Bukkit.getServer())
                        .getServer().registryAccess();
        return registries.lookupOrThrow(Registries.ENCHANTMENT)
                .get(registries.lookupOrThrow(Registries.ENCHANTMENT)
                        .getId(this.getHandle()))
                .map(h -> h.is(tag))
                .orElse(false);
    }

    @Unique
    public Component description() {
        return paperarc$adventure(this.getHandle().description());
    }

    @Unique
    public Component displayName(int level) {
        net.minecraft.core.RegistryAccess registries =
                ((org.bukkit.craftbukkit.v.CraftServer) org.bukkit.Bukkit.getServer())
                        .getServer().registryAccess();
        Holder<net.minecraft.world.item.enchantment.Enchantment> holder =
                registries.lookupOrThrow(Registries.ENCHANTMENT).wrapAsHolder(this.getHandle());
        return paperarc$adventure(
                net.minecraft.world.item.enchantment.Enchantment.getFullname(holder, level));
    }

    @Unique
    public Set<EquipmentSlotGroup> getActiveSlotGroups() {
        Set<EquipmentSlotGroup> out = new LinkedHashSet<>();
        for (net.minecraft.world.entity.EquipmentSlotGroup group : this.getHandle().definition().slots()) {
            EquipmentSlotGroup bukkit = CraftEquipmentSlot.getSlot(group);
            if (bukkit != null) {
                out.add(bukkit);
            }
        }
        return java.util.Collections.unmodifiableSet(out);
    }

    @Unique
    public int getAnvilCost() {
        return this.getHandle().getAnvilCost();
    }

    @Unique
    public float getDamageIncrease(int level, EntityCategory entityCategory) {
        return com.ixnah.mc.paperarc.bridge.api.PaperarcEnchantmentDamage.byCategory(
                ((org.bukkit.enchantments.Enchantment) (Object) this).getKey(), level, entityCategory);
    }

    @Unique
    public float getDamageIncrease(int level, EntityType entityType) {
        return com.ixnah.mc.paperarc.bridge.api.PaperarcEnchantmentDamage.byEntityType(
                ((org.bukkit.enchantments.Enchantment) (Object) this).getKey(), level, entityType);
    }

    @Unique
    public RegistryKeySet<org.bukkit.enchantments.Enchantment> getExclusiveWith() {
        List<TypedKey<org.bukkit.enchantments.Enchantment>> keys = new ArrayList<>();
        for (Holder<net.minecraft.world.item.enchantment.Enchantment> holder : this.getHandle().exclusiveSet()) {
            holder.unwrapKey().ifPresent(rk -> keys.add(TypedKey.create(RegistryKey.ENCHANTMENT,
                    net.kyori.adventure.key.Key.key(rk.identifier().getNamespace(), rk.identifier().getPath()))));
        }
        return RegistrySet.keySet(RegistryKey.ENCHANTMENT, keys);
    }

    @Unique
    public int getMaxModifiedCost(int level) {
        return this.getHandle().getMaxCost(level);
    }

    @Unique
    public int getMinModifiedCost(int level) {
        return this.getHandle().getMinCost(level);
    }

    @Unique
    public RegistryKeySet<ItemType> getPrimaryItems() {
        return paperarc$items(this.getHandle().definition().primaryItems().orElse(null));
    }

    @Unique
    public EnchantmentRarity getRarity() {
        int weight = this.getHandle().getWeight();
        if (weight >= 10) {
            return EnchantmentRarity.COMMON;
        }
        if (weight >= 5) {
            return EnchantmentRarity.UNCOMMON;
        }
        if (weight >= 2) {
            return EnchantmentRarity.RARE;
        }
        return EnchantmentRarity.VERY_RARE;
    }

    @Unique
    public RegistryKeySet<ItemType> getSupportedItems() {
        return paperarc$items(this.getHandle().definition().supportedItems());
    }

    @Unique
    public int getWeight() {
        return this.getHandle().getWeight();
    }

    @Unique
    public boolean isDiscoverable() {
        return this.paperarc$hasTag(EnchantmentTags.IN_ENCHANTING_TABLE);
    }

    @Unique
    public boolean isTradeable() {
        return this.paperarc$hasTag(EnchantmentTags.TRADEABLE);
    }

    @Unique
    public String translationKey() {
        net.minecraft.network.chat.Component description = this.getHandle().description();
        if (description.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents tc) {
            return tc.getKey();
        }
        return description.getString();
    }
}
