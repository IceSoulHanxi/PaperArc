package dev.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.scoreboard.Score} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.scoreboard.Score", remap = false)
public interface ScoreIfaceMixin {

    @Unique
    public abstract boolean isTriggerable();

    @Unique
    public abstract void setTriggerable(boolean p0);

    @Unique
    public abstract net.kyori.adventure.text.Component customName();

    @Unique
    public abstract void customName(net.kyori.adventure.text.Component p0);

    @Unique
    public abstract io.papermc.paper.scoreboard.numbers.NumberFormat numberFormat();

    @Unique
    public abstract void numberFormat(io.papermc.paper.scoreboard.numbers.NumberFormat p0);
}
