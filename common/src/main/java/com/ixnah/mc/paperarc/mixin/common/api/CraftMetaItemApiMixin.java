package com.ixnah.mc.paperarc.mixin.common.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
 * <p>键存在物品 NBT 里（{@code CanPlaceOn}/{@code CanDestroy} 字符串列表，与 Paper
 * 和 vanilla 冒险模式用的是同一份 NBT），落点是 CB 的 {@code unhandledTags}：
 * {@code CraftMetaItem} 对未识别的标签本来就有"读进来 / applyToItem 写回去 /
 * 参与 equals、clone、serialize"的完整通路，等于免费拿到持久化。
 * 原实现放在 Craft 侧注入字段，而 {@code CraftItemStack#getItemMeta()} 每次新建
 * {@code CraftMetaItem}（{@code javap -c} 核对：走静态工厂），所以
 * {@code setItemMeta} 一个来回就全丢，也不会真的限制冒险模式放置/破坏
 * （checklist §1.10 an，A6/X-1 复核）。</p>
 *
 * <p>Adventure {@code displayName()/lore()} convert via the legacy section serializer
 * like Paper's {@code PaperAdventure.LEGACY_SECTION_UX}; bungee {@code BaseComponent}
 * variants round-trip through gson.</p>
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

    @org.spongepowered.asm.mixin.Shadow
    @org.spongepowered.asm.mixin.Final
    Map<String, net.minecraft.nbt.Tag> unhandledTags;

    @Unique
    private static final String PAPERARC$CAN_PLACE_ON = "CanPlaceOn";

    @Unique
    private static final String PAPERARC$CAN_DESTROY = "CanDestroy";

    /** 从 unhandledTags 里的字符串列表读出键集合（顺序稳定，便于往返比较）。 */
    @Unique
    private Set<Namespaced> paperarc$readKeys(String nbtKey) {
        Set<Namespaced> keys = new LinkedHashSet<>();
        net.minecraft.nbt.Tag tag = this.unhandledTags.get(nbtKey);
        if (tag instanceof net.minecraft.nbt.ListTag) {
            net.minecraft.nbt.ListTag list = (net.minecraft.nbt.ListTag) tag;
            for (int i = 0; i < list.size(); i++) {
                Namespaced parsed = paperarc$deserializeNamespaced(list.getString(i));
                if (parsed != null) {
                    keys.add(parsed);
                }
            }
        }
        return keys;
    }

    @Unique
    private void paperarc$writeKeys(String nbtKey, Collection<Namespaced> keys) {
        if (keys == null || keys.isEmpty()) {
            this.unhandledTags.remove(nbtKey);
            return;
        }
        net.minecraft.nbt.ListTag list = new net.minecraft.nbt.ListTag();
        for (Namespaced key : keys) {
            list.add(net.minecraft.nbt.StringTag.valueOf(key.toString()));
        }
        this.unhandledTags.put(nbtKey, list);
    }

    /** 与 Paper 的 deserializeNamespaced 一致：用 BlockStateParser 校验并归一化。 */
    @Unique
    private static Namespaced paperarc$deserializeNamespaced(String raw) {
        boolean isTag = !raw.isEmpty() && raw.codePointAt(0) == '#';
        com.mojang.datafixers.util.Either<
                net.minecraft.commands.arguments.blocks.BlockStateParser.BlockResult,
                net.minecraft.commands.arguments.blocks.BlockStateParser.TagResult> result;
        try {
            result = net.minecraft.commands.arguments.blocks.BlockStateParser.parseForTesting(
                    net.minecraft.core.registries.BuiltInRegistries.BLOCK.asLookup(), raw, false);
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            return null;
        }
        net.minecraft.resources.ResourceLocation key = null;
        if (isTag && result.right().isPresent()
                && result.right().get().tag() instanceof net.minecraft.core.HolderSet.Named) {
            key = ((net.minecraft.core.HolderSet.Named<net.minecraft.world.level.block.Block>)
                    result.right().get().tag()).key().location();
        } else if (result.left().isPresent()) {
            key = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                    .getKey(result.left().get().blockState().getBlock());
        }
        if (key == null) {
            return null;
        }
        try {
            return isTag ? new NamespacedTag(key.getNamespace(), key.getPath())
                    : org.bukkit.NamespacedKey.fromString(key.toString());
        } catch (IllegalArgumentException ex) {
            org.bukkit.Bukkit.getLogger().warning("Namespaced resource does not validate: " + key);
            return null;
        }
    }

    // ------------------------------------------------------------------
    // CanPlaceOn / CanDestroy (legacy Material + Namespaced keys)
    // ------------------------------------------------------------------

    @Unique
    public Set<Material> getCanDestroy() {
        return legacyGetMatsFromKeys(this.paperarc$readKeys(PAPERARC$CAN_DESTROY));
    }

    @Unique
    public void setCanDestroy(Set<Material> canDestroy) {
        Preconditions.checkArgument(canDestroy != null, "Cannot replace with null set!");
        this.paperarc$writeKeys(PAPERARC$CAN_DESTROY, legacyToKeys(canDestroy));
    }

    @Unique
    public Set<Material> getCanPlaceOn() {
        return legacyGetMatsFromKeys(this.paperarc$readKeys(PAPERARC$CAN_PLACE_ON));
    }

    @Unique
    public void setCanPlaceOn(Set<Material> canPlaceOn) {
        Preconditions.checkArgument(canPlaceOn != null, "Cannot replace with null set!");
        this.paperarc$writeKeys(PAPERARC$CAN_PLACE_ON, legacyToKeys(canPlaceOn));
    }

    @Unique
    public Set<Namespaced> getDestroyableKeys() {
        return this.paperarc$readKeys(PAPERARC$CAN_DESTROY);
    }

    @Unique
    public void setDestroyableKeys(Collection<Namespaced> canDestroy) {
        Preconditions.checkArgument(canDestroy != null, "Cannot replace with null collection!");
        Preconditions.checkArgument(ofAcceptableType(canDestroy),
                "Can only use NamespacedKey or NamespacedTag objects!");
        this.paperarc$writeKeys(PAPERARC$CAN_DESTROY, canDestroy);
    }

    @Unique
    public Set<Namespaced> getPlaceableKeys() {
        return this.paperarc$readKeys(PAPERARC$CAN_PLACE_ON);
    }

    @Unique
    public void setPlaceableKeys(Collection<Namespaced> canPlaceOn) {
        Preconditions.checkArgument(canPlaceOn != null, "Cannot replace with null collection!");
        Preconditions.checkArgument(ofAcceptableType(canPlaceOn),
                "Can only use NamespacedKey or NamespacedTag objects!");
        this.paperarc$writeKeys(PAPERARC$CAN_PLACE_ON, canPlaceOn);
    }

    @Unique
    public boolean hasPlaceableKeys() {
        return this.unhandledTags.get(PAPERARC$CAN_PLACE_ON) instanceof net.minecraft.nbt.ListTag;
    }

    @Unique
    public boolean hasDestroyableKeys() {
        return this.unhandledTags.get(PAPERARC$CAN_DESTROY) instanceof net.minecraft.nbt.ListTag;
    }

    @Unique
    private Set<Namespaced> legacyToKeys(Collection<Material> beingSet) {
        if (beingSet.stream().anyMatch(Material::isLegacy)) {
            throw new IllegalArgumentException("Set must not contain any legacy materials!");
        }
        Set<Namespaced> keys = new LinkedHashSet<>();
        for (Material material : beingSet) {
            keys.add(material.getKey());
        }
        return keys;
    }

    @Unique
    private Set<Material> legacyGetMatsFromKeys(Collection<Namespaced> names) {
        Set<Material> mats = new LinkedHashSet<>();
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
