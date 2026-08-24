package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.scoreboard.Objective} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.scoreboard.Objective", remap = false)
public interface ObjectiveIfaceMixin {

    @Unique
    public abstract net.kyori.adventure.text.Component displayName();

    @Unique
    public abstract void displayName(net.kyori.adventure.text.Component p0);

    @Unique
    public abstract boolean willAutoUpdateDisplay();

    @Unique
    public abstract void setAutoUpdateDisplay(boolean p0);

    @Unique
    public abstract io.papermc.paper.scoreboard.numbers.NumberFormat numberFormat();

    @Unique
    public abstract void numberFormat(io.papermc.paper.scoreboard.numbers.NumberFormat p0);
}
