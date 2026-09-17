package com.ixnah.mc.paperarc.mixin.common.api;

import io.papermc.paper.plugin.configuration.PluginMeta;

import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's {@code Plugin#getPluginMeta()} to the runtime {@link JavaPlugin}.
 *
 * <p>Arclight's {@code JavaPlugin} does not implement {@code PluginMeta} (Paper makes
 * {@code PluginDescriptionFile} implement it), so the meta cannot be cast directly.
 * Returns {@code null} as a safe degradation — plugin-metadata introspection through
 * this Paper entry point is not supported on the Arclight plugin loader.</p>
 */
@Mixin(JavaPlugin.class)
public abstract class JavaPluginApiMixin {

    @Shadow
    public abstract org.bukkit.plugin.PluginDescriptionFile getDescription();

    @Unique
    public PluginMeta getPluginMeta() {
        // Arclight PluginDescriptionFile does not implement io.papermc.paper.plugin.configuration.PluginMeta
        return null;
    }
}
