package com.ixnah.mc.paperarc.mixin.common.api;

import io.papermc.paper.plugin.configuration.PluginMeta;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's {@code PluginManager#isTransitiveDependency}/{@code overridePermissionManager}
 * to the runtime {@link SimplePluginManager}.
 *
 * <p>The Arclight plugin loader has no notion of Paper's plugin-dependency-graph
 * queries, so {@code isTransitiveDependency} returns {@code false} and
 * {@code overridePermissionManager} is accepted but ignored (permission lookups still
 * route through the vanilla {@code SimplePluginManager} machinery).</p>
 */
@Mixin(targets = "org.bukkit.plugin.SimplePluginManager", remap = false)
public abstract class SimplePluginManagerApiMixin {

    @Unique
    public boolean isTransitiveDependency(PluginMeta depend, PluginMeta dependency) {
        return false;
    }

    @Unique
    public void overridePermissionManager(Plugin plugin, io.papermc.paper.plugin.PermissionManager manager) {
        // no-op: Arclight routes permissions through SimplePluginManager
    }
}
