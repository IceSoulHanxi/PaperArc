package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.ServerLinks}.
 * Adds paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).
 */
@Mixin(targets = "org.bukkit.ServerLinks", remap = false)
public interface ServerLinksIfaceMixin {

    @Unique
    public abstract org.bukkit.ServerLinks.ServerLink addLink(net.kyori.adventure.text.Component p0, java.net.URI p1);
}
