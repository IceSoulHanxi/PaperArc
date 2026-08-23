package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.scoreboard.Team} (generated).
 * Adds 10 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.scoreboard.Team", remap = false)
public interface TeamIfaceMixin {

    public abstract net.kyori.adventure.text.Component displayName();

    public abstract void displayName(net.kyori.adventure.text.Component p0);

    public abstract net.kyori.adventure.text.Component prefix();

    public abstract void prefix(net.kyori.adventure.text.Component p0);

    public abstract net.kyori.adventure.text.Component suffix();

    public abstract void suffix(net.kyori.adventure.text.Component p0);

    public abstract boolean hasColor();

    public abstract net.kyori.adventure.text.format.TextColor color();

    public abstract void color(net.kyori.adventure.text.format.NamedTextColor p0);

    public abstract void addEntities(java.util.Collection p0);
}
