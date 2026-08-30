package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.inventory.meta.ItemMeta} (generated).
 * Adds 18 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.inventory.meta.ItemMeta", remap = false)
public interface ItemMetaIfaceMixin {

    @Unique
    public abstract boolean hasDestroyableKeys();

    @Unique
    public abstract boolean hasPlaceableKeys();

    @Unique
    public abstract java.util.List<net.kyori.adventure.text.Component> lore();

    @Unique
    public abstract java.util.List<net.md_5.bungee.api.chat.BaseComponent[]> getLoreComponents();

    @Unique
    public abstract java.util.Set<com.destroystokyo.paper.Namespaced> getDestroyableKeys();

    @Unique
    public abstract java.util.Set<com.destroystokyo.paper.Namespaced> getPlaceableKeys();

    @Unique
    public abstract java.util.Set<org.bukkit.Material> getCanDestroy();

    @Unique
    public abstract java.util.Set<org.bukkit.Material> getCanPlaceOn();

    @Unique
    public abstract net.kyori.adventure.text.Component displayName();

    @Unique
    public abstract net.md_5.bungee.api.chat.BaseComponent[] getDisplayNameComponent();

    @Unique
    public abstract void displayName(net.kyori.adventure.text.Component p0);

    @Unique
    public abstract void lore(java.util.List<? extends net.kyori.adventure.text.Component> p0);

    @Unique
    public abstract void setCanDestroy(java.util.Set<org.bukkit.Material> p0);

    @Unique
    public abstract void setCanPlaceOn(java.util.Set<org.bukkit.Material> p0);

    @Unique
    public abstract void setDestroyableKeys(java.util.Collection<com.destroystokyo.paper.Namespaced> p0);

    @Unique
    public abstract void setDisplayNameComponent(net.md_5.bungee.api.chat.BaseComponent[] p0);

    @Unique
    public abstract void setLoreComponents(java.util.List<net.md_5.bungee.api.chat.BaseComponent[]> p0);

    @Unique
    public abstract void setPlaceableKeys(java.util.Collection<com.destroystokyo.paper.Namespaced> p0);

}