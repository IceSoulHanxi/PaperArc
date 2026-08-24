package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Minecart} (generated).
 * Adds 1 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Minecart", remap = false)
public interface MinecartIfaceMixin {

    @Unique
    public abstract org.bukkit.Material getMinecartMaterial();
}
