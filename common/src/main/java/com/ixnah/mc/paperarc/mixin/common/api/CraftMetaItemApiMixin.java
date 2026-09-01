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

/**
 * Adds Paper's CanPlaceOn/CanDestroy NBT API and the adventure {@code Component}
 * variants to the craft {@code CraftMetaItem} (package-private).
 *
 * <p>The NBT read/write integration (CAN_DESTROY/CAN_PLACE_ON {@code ItemMetaKey}s,
 * HANDLED_TAGS, BlockStateParser) is Paper-patch internals not present in Arclight;
 * here the keys are kept in-memory only and the legacy {@code getCanDestroy/
 * setCanDestroy/getCanPlaceOn/setCanPlaceOn} plus {@code ...Keys} accessors mirror
 * the Paper semantics. Adventure {@code displayName()/lore()} convert via the legacy
 * section serializer like Paper's {@code PaperAdventure.LEGACY_SECTION_UX}; bungee
 * {@code BaseComponent} variants round-trip through gson.</p>
 */
@Mixin(targets = "org.bukkit.craftbukkit.v.inventory.CraftMetaItem")
public abstract class CraftMetaItemApiMixin {

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
