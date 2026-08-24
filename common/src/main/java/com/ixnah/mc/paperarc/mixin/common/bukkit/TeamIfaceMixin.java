package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.scoreboard.Team} (generated).
 * Adds 10 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.scoreboard.Team", remap = false)
public interface TeamIfaceMixin {

    @Unique
    public abstract net.kyori.adventure.text.Component displayName();

    @Unique
    public abstract void displayName(net.kyori.adventure.text.Component p0);

    @Unique
    public abstract net.kyori.adventure.text.Component prefix();

    @Unique
    public abstract void prefix(net.kyori.adventure.text.Component p0);

    @Unique
    public abstract net.kyori.adventure.text.Component suffix();

    @Unique
    public abstract void suffix(net.kyori.adventure.text.Component p0);

    @Unique
    public abstract boolean hasColor();

    @Unique
    public abstract net.kyori.adventure.text.format.TextColor color();

    @Unique
    public abstract void color(net.kyori.adventure.text.format.NamedTextColor p0);

    @Unique
    public abstract void addEntities(java.util.Collection p0);
}
