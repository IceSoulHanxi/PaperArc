package com.ixnah.mc.paperarc.mixin.common.api;

import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's {@code CommandMap#getKnownCommands()} to the runtime
 * {@link SimpleCommandMap} (org.bukkit.command, present in the Arclight jar but
 * missing the Paper-injected method). Mirrors
 * {@code command-map.getKnownCommands.patch} which simply exposes the backing map.
 */
@Mixin(targets = "org.bukkit.command.SimpleCommandMap", remap = false)
public abstract class SimpleCommandMapApiMixin {

    @Shadow(remap = false)
    protected Map<String, Command> knownCommands;

    @Unique
    public Map<String, Command> getKnownCommands() {
        return this.knownCommands;
    }
}
