package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.scoreboard.Objective} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.scoreboard.Objective", remap = false)
public interface ObjectiveIfaceMixin {

    public abstract net.kyori.adventure.text.Component displayName();

    public abstract void displayName(net.kyori.adventure.text.Component p0);

    public abstract boolean willAutoUpdateDisplay();

    public abstract void setAutoUpdateDisplay(boolean p0);

    public abstract io.papermc.paper.scoreboard.numbers.NumberFormat numberFormat();

    public abstract void numberFormat(io.papermc.paper.scoreboard.numbers.NumberFormat p0);
}
