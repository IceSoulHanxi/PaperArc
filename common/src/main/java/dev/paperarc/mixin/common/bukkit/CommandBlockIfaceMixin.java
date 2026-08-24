package dev.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.block.CommandBlock} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.block.CommandBlock", remap = false)
public interface CommandBlockIfaceMixin {

    @Unique
    public abstract net.kyori.adventure.text.Component name();

    @Unique
    public abstract void name(net.kyori.adventure.text.Component p0);
}
