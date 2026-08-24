package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.TextDisplay} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.TextDisplay", remap = false)
public interface TextDisplayIfaceMixin {

    @Unique
    public abstract net.kyori.adventure.text.Component text();

    @Unique
    public abstract void text(net.kyori.adventure.text.Component p0);
}
