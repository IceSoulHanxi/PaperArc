package com.ixnah.mc.paperarc.mixin.common.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.destroystokyo.paper.Namespaced;
import com.destroystokyo.paper.NamespacedTag;
import com.google.common.base.Preconditions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

/**
 * Adds Paper's CanPlaceOn/CanDestroy NBT API and the adventure {@code Component}
 * variants to the craft {@code CraftMetaItem} (package-private).
 *
 * <p><b>B8/Y-4（gaps.md G.1）</b>：两组键不再只存在内存里 —— 1.21.1 的落点是数据组件
 * {@code minecraft:can_place_on} / {@code minecraft:can_break}，由
 * {@code api.CraftItemStackAdventureModeMixin} 在 {@code CraftItemStack} 的
 * {@code setItemMeta}/{@code getItemMeta} 两头读写，所以设进去的键真的会影响
 * 冒险模式的放置/破坏判定，也随物品一起存档。legacy {@code getCanDestroy/
 * setCanDestroy/getCanPlaceOn/setCanPlaceOn} plus {@code ...Keys} accessors mirror
 * the Paper semantics. Adventure {@code displayName()/lore()} convert via the legacy
 * section serializer like Paper's {@code PaperAdventure.LEGACY_SECTION_UX}; bungee
 * {@code BaseComponent} variants round-trip through gson.</p>
 */
@Mixin(targets = "org.bukkit.craftbukkit.v.inventory.CraftMetaItem")
public abstract class CraftMetaItemApiMixin
        implements com.ixnah.mc.paperarc.bridge.craft.CraftMetaItemAdventureBridge {

    @org.spongepowered.asm.mixin.Shadow
    public abstract String getDisplayName();

    @org.spongepowered.asm.mixin.Shadow
    public abstract void setDisplayName(String displayName);

    @org.spongepowered.asm.mixin.Shadow
    public abstract java.util.List<String> getLore();

    @org.spongepowered.asm.mixin.Shadow
    public abstract void setLore(java.util.List<String> lore);

    @Unique
    private Set<Namespaced> placeableKeys = new HashSet<>();

    @Unique
    private Set<Namespaced> destroyableKeys = new HashSet<>();

    @Override
    public Set<Namespaced> paperarc$placeableKeys() {
        return this.placeableKeys;
    }

    @Override
    public Set<Namespaced> paperarc$destroyableKeys() {
        return this.destroyableKeys;
    }

    @Override
    public void paperarc$setPlaceableKeys(Collection<Namespaced> keys) {
        this.placeableKeys.clear();
        this.placeableKeys.addAll(keys);
    }

    @Override
    public void paperarc$setDestroyableKeys(Collection<Namespaced> keys) {
        this.destroyableKeys.clear();
        this.destroyableKeys.addAll(keys);
    }

    /**
     * 复制构造器要把两组键带过去。
     *
     * <p>{@code ItemStack#setItemMeta} 并不会把插件给的那个 meta 直接存下来 ——
     * 它走 {@code CraftItemFactory#asMetaFor}，也就是 {@code new CraftMetaItem(meta)}
     * 复制一份；{@code clone()} 同理。vanilla 的复制构造器当然不认识我们
     * {@code @Unique} 加的两个字段，不补这一条，键在 {@code setItemMeta} 那一步就没了
     * （探针 P40 实测 {@code hasItemMeta=false inMemory=[]}）。
     */
    @Inject(method = "<init>(Lorg/bukkit/craftbukkit/v/inventory/CraftMetaItem;)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$copyAdventureKeys(org.bukkit.craftbukkit.v.inventory.CraftMetaItem meta,
                                            org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (meta instanceof com.ixnah.mc.paperarc.bridge.craft.CraftMetaItemAdventureBridge source) {
            this.placeableKeys.addAll(source.paperarc$placeableKeys());
            this.destroyableKeys.addAll(source.paperarc$destroyableKeys());
        }
    }

    /**
     * 有 CanPlaceOn/CanDestroy 就不算空 meta。
     *
     * <p>不改这条，落盘就是空跑：{@code ItemStack#hasItemMeta()} 与
     * {@code CraftItemStack#setItemMeta} 都先问 {@code CraftMetaItem#isEmpty()}，
     * 只带这两组键的 meta 被判成"空"后压根不会走到写物品那一步
     * （探针 P40 的序列化往返当场抓到：两边都是 {@code []}）。Paper 同样在
     * {@code isEmpty()} 里算上这两组键。
     */
    @com.llamalad7.mixinextras.injector.ModifyReturnValue(method = "isEmpty", at = @At("RETURN"), remap = false)
    private boolean paperarc$notEmptyWithAdventureKeys(boolean empty) {
        return empty && !this.hasPlaceableKeys() && !this.hasDestroyableKeys();
    }

    // ------------------------------------------------------------------
    // CanPlaceOn / CanDestroy (legacy Material + Namespaced keys)
    // ------------------------------------------------------------------

    @Unique
    public Set<Material> getCanDestroy() {
        return !hasDestroyableKeys() ? Collections.emptySet() : legacyGetMatsFromKeys(this.destroyableKeys);
    }

    @Unique
    public void setCanDestroy(Set<Material> canDestroy) {
        Preconditions.checkArgument(canDestroy != null, "Cannot replace with null set!");
        legacyClearAndReplaceKeys(this.destroyableKeys, canDestroy);
    }

    @Unique
    public Set<Material> getCanPlaceOn() {
        return !hasPlaceableKeys() ? Collections.emptySet() : legacyGetMatsFromKeys(this.placeableKeys);
    }

    @Unique
    public void setCanPlaceOn(Set<Material> canPlaceOn) {
        Preconditions.checkArgument(canPlaceOn != null, "Cannot replace with null set!");
        legacyClearAndReplaceKeys(this.placeableKeys, canPlaceOn);
    }

    @Unique
    public Set<Namespaced> getDestroyableKeys() {
        return !hasDestroyableKeys() ? Collections.emptySet() : new HashSet<>(this.destroyableKeys);
    }

    @Unique
    public void setDestroyableKeys(Collection<Namespaced> canDestroy) {
        Preconditions.checkArgument(canDestroy != null, "Cannot replace with null collection!");
        Preconditions.checkArgument(ofAcceptableType(canDestroy),
                "Can only use NamespacedKey or NamespacedTag objects!");
        this.destroyableKeys.clear();
        this.destroyableKeys.addAll(canDestroy);
    }

    @Unique
    public Set<Namespaced> getPlaceableKeys() {
        return !hasPlaceableKeys() ? Collections.emptySet() : new HashSet<>(this.placeableKeys);
    }

    @Unique
    public void setPlaceableKeys(Collection<Namespaced> canPlaceOn) {
        Preconditions.checkArgument(canPlaceOn != null, "Cannot replace with null collection!");
        Preconditions.checkArgument(ofAcceptableType(canPlaceOn),
                "Can only use NamespacedKey or NamespacedTag objects!");
        this.placeableKeys.clear();
        this.placeableKeys.addAll(canPlaceOn);
    }

    @Unique
    public boolean hasPlaceableKeys() {
        return this.placeableKeys != null && !this.placeableKeys.isEmpty();
    }

    @Unique
    public boolean hasDestroyableKeys() {
        return this.destroyableKeys != null && !this.destroyableKeys.isEmpty();
    }

    @Unique
    private void legacyClearAndReplaceKeys(Collection<Namespaced> toUpdate, Collection<Material> beingSet) {
        if (beingSet.stream().anyMatch(Material::isLegacy)) {
            throw new IllegalArgumentException("Set must not contain any legacy materials!");
        }
        toUpdate.clear();
        for (Material material : beingSet) {
            toUpdate.add(material.getKey());
        }
    }

    @Unique
    private Set<Material> legacyGetMatsFromKeys(Collection<Namespaced> names) {
        Set<Material> mats = new HashSet<>();
        for (Namespaced key : names) {
            if (!(key instanceof org.bukkit.NamespacedKey)) {
                continue;
            }
            Material material = Material.matchMaterial(key.toString(), false);
            if (material != null) {
                mats.add(material);
            }
        }
        return mats;
    }

    @Unique
    private boolean ofAcceptableType(Collection<Namespaced> namespacedResources) {
        for (Namespaced resource : namespacedResources) {
            if (!(resource instanceof org.bukkit.NamespacedKey || resource instanceof NamespacedTag)) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Adventure Component display name & lore
    // ------------------------------------------------------------------

    @Unique
    public Component displayName() {
        return getDisplayName() == null ? null
                : LegacyComponentSerializer.legacySection().deserialize(getDisplayName());
    }

    @Unique
    public void displayName(Component component) {
        setDisplayName(component == null ? null
                : LegacyComponentSerializer.legacySection().serialize(component));
    }

    @Unique
    public java.util.List<Component> lore() {
        List<String> legacy = getLore();
        if (legacy == null) {
            return null;
        }
        List<Component> components = new ArrayList<>(legacy.size());
        for (String line : legacy) {
            components.add(LegacyComponentSerializer.legacySection().deserialize(line));
        }
        return components;
    }

    @Unique
    public void lore(List<? extends Component> lore) {
        if (lore == null) {
            setLore(null);
            return;
        }
        List<String> legacy = new ArrayList<>(lore.size());
        for (Component component : lore) {
            legacy.add(LegacyComponentSerializer.legacySection().serialize(component));
        }
        setLore(legacy);
    }

    // ------------------------------------------------------------------
    // Bungee BaseComponent display name & lore
    // ------------------------------------------------------------------

    @Unique
    public net.md_5.bungee.api.chat.BaseComponent[] getDisplayNameComponent() {
        Component component = displayName();
        return component == null ? null
                : net.md_5.bungee.chat.ComponentSerializer.parse(
                        GsonComponentSerializer.gson().serialize(component));
    }

    @Unique
    public void setDisplayNameComponent(net.md_5.bungee.api.chat.BaseComponent[] component) {
        setDisplayName(component == null ? null
                : LegacyComponentSerializer.legacySection().serialize(
                        GsonComponentSerializer.gson().deserialize(
                                net.md_5.bungee.chat.ComponentSerializer.toString(component))));
    }

    @Unique
    public List<net.md_5.bungee.api.chat.BaseComponent[]> getLoreComponents() {
        List<Component> components = lore();
        if (components == null) {
            return null;
        }
        List<net.md_5.bungee.api.chat.BaseComponent[]> bungee = new ArrayList<>(components.size());
        for (Component component : components) {
            bungee.add(net.md_5.bungee.chat.ComponentSerializer.parse(
                    GsonComponentSerializer.gson().serialize(component)));
        }
        return bungee;
    }

    @Unique
    public void setLoreComponents(List<net.md_5.bungee.api.chat.BaseComponent[]> lore) {
        if (lore == null) {
            setLore(null);
            return;
        }
        List<String> legacy = new ArrayList<>(lore.size());
        for (net.md_5.bungee.api.chat.BaseComponent[] component : lore) {
            legacy.add(LegacyComponentSerializer.legacySection().serialize(
                    GsonComponentSerializer.gson().deserialize(
                            net.md_5.bungee.chat.ComponentSerializer.toString(component))));
        }
        setLore(legacy);
    }
}
