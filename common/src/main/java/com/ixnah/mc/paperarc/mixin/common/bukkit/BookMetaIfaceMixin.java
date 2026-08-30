package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.inventory.meta.BookMeta} (generated).
 * Adds 8 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.inventory.meta.BookMeta", remap = false)
public interface BookMetaIfaceMixin {

    @Unique
    public abstract net.kyori.adventure.text.Component author();

    @Unique
    public abstract net.kyori.adventure.text.Component page(int p0);

    @Unique
    public abstract net.kyori.adventure.text.Component title();

    @Unique
    public abstract org.bukkit.inventory.meta.BookMeta author(net.kyori.adventure.text.Component p0);

    @Unique
    public abstract org.bukkit.inventory.meta.BookMeta title(net.kyori.adventure.text.Component p0);

    @Unique
    public abstract org.bukkit.inventory.meta.BookMeta.BookMetaBuilder toBuilder();

    @Unique
    public abstract void addPages(net.kyori.adventure.text.Component... p0);

    @Unique
    public abstract void page(int p0, net.kyori.adventure.text.Component p1);

}