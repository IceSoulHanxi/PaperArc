package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.attribute.AttributeInstance} (generated, trimmed for 1.20.1).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.attribute.AttributeInstance", remap = false)
public interface AttributeInstanceIfaceMixin {

    @Unique
    public abstract void removeModifier(net.kyori.adventure.key.Key p0);

    @Unique
    public abstract void addTransientModifier(org.bukkit.attribute.AttributeModifier p0);
}
