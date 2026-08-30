package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.plugin.PluginManager} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.plugin.PluginManager", remap = false)
public interface PluginManagerIfaceMixin {

    @Unique
    public abstract boolean isTransitiveDependency(io.papermc.paper.plugin.configuration.PluginMeta p0, io.papermc.paper.plugin.configuration.PluginMeta p1);

    @Unique
    public abstract void overridePermissionManager(org.bukkit.plugin.Plugin p0, io.papermc.paper.plugin.PermissionManager p1);

}