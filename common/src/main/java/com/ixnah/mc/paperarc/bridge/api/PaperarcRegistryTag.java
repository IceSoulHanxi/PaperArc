package com.ixnah.mc.paperarc.bridge.api;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v.CraftServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * paper 的 {@code UnsafeValues#getTag(TagKey)} 的返回值：一个只读的
 * {@code io.papermc.paper.registry.tag.Tag}，内容直接来自 vanilla 注册表的同名标签。
 *
 * <p>paper 的 {@code RegistryKey} 与 vanilla 注册表 id 是同一套命名空间键
 *（{@code minecraft:item}、{@code minecraft:block}…），所以不需要 paper 那套 RegistryAccess，
 * 按键名反查 vanilla 注册表即可。{@code resolve(org.bukkit.Registry)} 把 {@code TypedKey}
 * 交给传进来的 Bukkit 注册表转成对象，转不到的跳过（标签里可能有 Arclight 侧没暴露的条目）。
 *
 * <p>类放在 bridge 里而不是 mixin 里：mixin 不能出现内部/匿名类（见 docs/mixin-conventions.md）。
 */
public final class PaperarcRegistryTag<A extends Keyed> implements Tag<A> {

    private final TagKey<A> tagKey;
    private final List<TypedKey<A>> values;

    private PaperarcRegistryTag(TagKey<A> tagKey, List<TypedKey<A>> values) {
        this.tagKey = tagKey;
        this.values = values;
    }

    /** 查不到注册表或标签时返回 null —— paper 的契约就是"没有就 null"。 */
    @SuppressWarnings("unchecked")
    public static <A extends Keyed> Tag<A> of(TagKey<A> tagKey) {
        if (tagKey == null) {
            return null;
        }
        MinecraftServer server = ((CraftServer) org.bukkit.Bukkit.getServer()).getServer();
        RegistryKey<A> registryKey = tagKey.registryKey();
        ResourceLocation registryId = ResourceLocation.parse(registryKey.key().asString());
        ResourceKey<? extends Registry<Object>> resourceKey = ResourceKey.createRegistryKey(registryId);
        Optional<Registry<Object>> registry = server.registryAccess().registry(resourceKey);
        if (registry.isEmpty()) {
            return null;
        }
        net.minecraft.tags.TagKey<Object> vanillaTag = net.minecraft.tags.TagKey.create(
                resourceKey, ResourceLocation.parse(tagKey.key().asString()));
        Optional<HolderSet.Named<Object>> holders = registry.get().getTag(vanillaTag);
        if (holders.isEmpty()) {
            return null;
        }
        List<TypedKey<A>> values = new ArrayList<>();
        for (Holder<Object> holder : holders.get()) {
            holder.unwrapKey().ifPresent(key -> values.add(TypedKey.create(
                    registryKey, Key.key(key.location().getNamespace(), key.location().getPath()))));
        }
        return new PaperarcRegistryTag<>(tagKey, Collections.unmodifiableList(values));
    }

    @Override
    public TagKey<A> tagKey() {
        return this.tagKey;
    }

    @Override
    public RegistryKey<A> registryKey() {
        return this.tagKey.registryKey();
    }

    @Override
    public Collection<TypedKey<A>> values() {
        return this.values;
    }

    @Override
    public Collection<A> resolve(org.bukkit.Registry<A> registry) {
        List<A> out = new ArrayList<>(this.values.size());
        for (TypedKey<A> key : this.values) {
            A value = registry.get(new NamespacedKey(key.key().namespace(), key.key().value()));
            if (value != null) {
                out.add(value);
            }
        }
        return Collections.unmodifiableList(out);
    }

    @Override
    public boolean contains(TypedKey<A> valueKey) {
        return this.values.contains(valueKey);
    }

    @Override
    public int size() {
        return this.values.size();
    }
}
