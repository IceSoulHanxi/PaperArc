package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.Nameable} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.Nameable", remap = false)
public interface NameableIfaceMixin {

    @Unique
    public abstract net.kyori.adventure.text.Component customName();

    @Unique
    public abstract void customName(net.kyori.adventure.text.Component p0);

}