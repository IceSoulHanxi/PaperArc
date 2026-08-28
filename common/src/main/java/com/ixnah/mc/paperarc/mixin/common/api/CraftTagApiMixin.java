package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.tag.CraftTag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's generic {@link org.bukkit.Tag} membership API on CraftTag.
 *
 * Membership is resolved purely via registry locations so it works for every
 * registry without per-type converters (Arclight has no CraftNamespacedKey#toMinecraft).
 */
@Mixin(CraftTag.class)
public abstract class CraftTagApiMixin {

    @Shadow
    public abstract net.minecraft.core.HolderSet.Named<?> getHandle();

    @Shadow
    @Final
    protected net.minecraft.core.Registry<?> registry;

    @Unique
    private static net.minecraft.resources.ResourceLocation paperarc$toResourceLocation(NamespacedKey key) {
        return new net.minecraft.resources.ResourceLocation(key.getNamespace(), key.getKey());
    }

    @Unique
    public boolean isTagged(Keyed item) {
        net.minecraft.resources.ResourceLocation location = paperarc$toResourceLocation(item.getKey());
        for (net.minecraft.core.Holder<?> holder : getHandle()) {
            java.util.Optional<? extends net.minecraft.resources.ResourceKey<?>> key = holder.unwrapKey();
            if (key.isPresent() && key.get().location().equals(location)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Paper: 返回标签当前全部值。基础 jar 的 CraftTag 没有 Paper 的逐类型转换器，
     * 这里把 NMS registry 位置映射到对应的 Bukkit Registry 单例后按 id 取值。
     * 无 Bukkit 对应物的 NMS registry 抛异常并带位置信息，让缺口显式暴露而不是静默丢值。
     */
    @Unique
    public java.util.Set<org.bukkit.Keyed> getValues() {
        org.bukkit.Registry<org.bukkit.Keyed> bukkitRegistry = paperarc$bukkitRegistry(this.registry);
        java.util.Set<org.bukkit.Keyed> values = new java.util.LinkedHashSet<>();
        for (net.minecraft.core.Holder<?> holder : getHandle()) {
            holder.unwrapKey().ifPresent(key -> {
                net.minecraft.resources.ResourceLocation location = key.location();
                Object value = bukkitRegistry.get(new NamespacedKey(location.getNamespace(), location.getPath()));
                if (value == null) {
                    throw new IllegalStateException("PaperArc CraftTag#getValues: registry "
                            + this.registry.key() + " has no Bukkit value for " + location);
                }
                values.add((org.bukkit.Keyed) value);
            });
        }
        return values;
    }

    /** NMS registry 位置 → Bukkit Registry 单例的懒加载映射（避免 mixin 内静态初始化块）。 */
    @Unique
    private static volatile java.util.Map<String, org.bukkit.Registry<? extends org.bukkit.Keyed>> paperarc$registryMap;

    @Unique
    @SuppressWarnings("unchecked")
    private static org.bukkit.Registry<org.bukkit.Keyed> paperarc$bukkitRegistry(net.minecraft.core.Registry<?> registry) {
        String location = registry.key().location().toString();
        java.util.Map<String, org.bukkit.Registry<? extends org.bukkit.Keyed>> map = paperarc$registryMap;
        if (map == null) {
            map = new java.util.HashMap<>();
            map.put("minecraft:item", org.bukkit.Registry.MATERIAL);
            map.put("minecraft:block", org.bukkit.Registry.MATERIAL);
            map.put("minecraft:entity_type", org.bukkit.Registry.ENTITY_TYPE);
            map.put("minecraft:fluid", org.bukkit.Registry.FLUID);
            map.put("minecraft:game_event", org.bukkit.Registry.GAME_EVENT);
            map.put("minecraft:mob_effect", org.bukkit.Registry.POTION_EFFECT_TYPE);
            map.put("minecraft:sound_event", org.bukkit.Registry.SOUNDS);
            map.put("minecraft:music_instrument", org.bukkit.Registry.INSTRUMENT);
            map.put("minecraft:trim_material", org.bukkit.Registry.TRIM_MATERIAL);
            map.put("minecraft:trim_pattern", org.bukkit.Registry.TRIM_PATTERN);
            map.put("minecraft:villager_profession", org.bukkit.Registry.VILLAGER_PROFESSION);
            map.put("minecraft:villager_type", org.bukkit.Registry.VILLAGER_TYPE);
            map.put("minecraft:attribute", org.bukkit.Registry.ATTRIBUTE);
            map.put("minecraft:memory_module_type", org.bukkit.Registry.MEMORY_MODULE_TYPE);
            map.put("minecraft:painting_variant", org.bukkit.Registry.ART);
            map.put("minecraft:frog_variant", org.bukkit.Registry.FROG_VARIANT);
            map.put("minecraft:worldgen/biome", org.bukkit.Registry.BIOME);
            map.put("minecraft:worldgen/structure", org.bukkit.Registry.STRUCTURE);
            map.put("minecraft:worldgen/structure_type", org.bukkit.Registry.STRUCTURE_TYPE);
            paperarc$registryMap = map;
        }
        org.bukkit.Registry<? extends org.bukkit.Keyed> mapped = map.get(location);
        if (mapped == null) {
            throw new UnsupportedOperationException(
                    "PaperArc CraftTag#getValues: no Bukkit Registry mapping for NMS registry " + location);
        }
        return (org.bukkit.Registry<org.bukkit.Keyed>) mapped;
    }
}
