package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.inventory.ItemFactory} (generated).
 * Adds 7 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.inventory.ItemFactory", remap = false)
public interface ItemFactoryIfaceMixin {

    @Unique
    public abstract net.kyori.adventure.text.event.HoverEvent asHoverEvent(org.bukkit.inventory.ItemStack p0, java.util.function.UnaryOperator p1);

    @Unique
    public abstract net.kyori.adventure.text.Component displayName(org.bukkit.inventory.ItemStack p0);

    @Unique
    public abstract java.lang.String getI18NDisplayName(org.bukkit.inventory.ItemStack p0);

    @Unique
    public abstract org.bukkit.inventory.ItemStack ensureServerConversions(org.bukkit.inventory.ItemStack p0);

    @Unique
    public abstract net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(org.bukkit.entity.Entity p0);

    @Unique
    public abstract net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(org.bukkit.entity.Entity p0, net.md_5.bungee.api.chat.BaseComponent p1);

    @Unique
    public abstract org.bukkit.inventory.ItemStack enchantWithLevels(org.bukkit.inventory.ItemStack p0, int p1, boolean p2, java.util.Random p3);
}
