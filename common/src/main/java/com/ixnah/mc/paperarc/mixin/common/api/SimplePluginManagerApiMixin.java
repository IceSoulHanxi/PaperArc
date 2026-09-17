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

    /** paper {@code PermissionManager#addPermissions}（B3-2）：批量注册，逐条走已有的 addPermission。 */
    @Unique
    public void addPermissions(java.util.List<org.bukkit.permissions.Permission> permissions) {
        org.bukkit.plugin.PluginManager self = (org.bukkit.plugin.PluginManager) (Object) this;
        for (org.bukkit.permissions.Permission permission : permissions) {
            self.addPermission(permission);
        }
    }

    /** paper {@code PermissionManager#clearPermissions}。 */
    @Unique
    public void clearPermissions() {
        org.bukkit.plugin.PluginManager self = (org.bukkit.plugin.PluginManager) (Object) this;
        for (org.bukkit.permissions.Permission permission :
                new java.util.ArrayList<>(self.getPermissions())) {
            self.removePermission(permission);
        }
    }
}
