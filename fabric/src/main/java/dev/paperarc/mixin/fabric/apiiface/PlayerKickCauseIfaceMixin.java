package dev.paperarc.mixin.fabric.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Fabric-only declaration of Paper's Player#kick(Component, Cause).
 * The PlayerKickEvent$Cause descriptor is unresolvable during NeoForge's
 * early mixin apply (arclight bukkit lacks the class), so it stays per-loader.
 */
@Mixin(targets = "org.bukkit.entity.Player", remap = false)
public interface PlayerKickCauseIfaceMixin {

    @Unique
    public abstract void kick(net.kyori.adventure.text.Component p0, org.bukkit.event.player.PlayerKickEvent.Cause p1);
}
