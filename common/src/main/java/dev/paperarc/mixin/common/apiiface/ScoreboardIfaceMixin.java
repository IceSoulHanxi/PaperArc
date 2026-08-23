package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.scoreboard.Scoreboard} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.scoreboard.Scoreboard", remap = false)
public interface ScoreboardIfaceMixin {

    public abstract org.bukkit.scoreboard.Objective registerNewObjective(java.lang.String p0, org.bukkit.scoreboard.Criteria p1, net.kyori.adventure.text.Component p2);

    public abstract org.bukkit.scoreboard.Objective registerNewObjective(java.lang.String p0, java.lang.String p1, net.kyori.adventure.text.Component p2, org.bukkit.scoreboard.RenderType p3);
}
