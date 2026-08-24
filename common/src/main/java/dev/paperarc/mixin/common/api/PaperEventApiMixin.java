package dev.paperarc.mixin.common.api;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.event.Event;
import org.bukkit.event.Cancellable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper adds {@code Event#callEvent()} to the Bukkit base class; spigot-based
 * runtimes (Arclight) don't have it, yet shaded Paper event subclasses and our
 * mixins call it everywhere. Merging it onto the runtime base class restores
 * Paper's firing convenience for every event.
 */
@Mixin(Event.class)
public abstract class PaperEventApiMixin {

    @Unique
    public boolean callEvent() {
        Event self = (Event) (Object) this;
        ((CraftServer) Bukkit.getServer()).getPluginManager().callEvent(self);
        return !(self instanceof Cancellable cancellable) || !cancellable.isCancelled();
    }
}
