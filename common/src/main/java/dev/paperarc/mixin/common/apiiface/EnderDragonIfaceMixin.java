package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.EnderDragon} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.EnderDragon", remap = false)
public interface EnderDragonIfaceMixin {

    @Unique
    public abstract org.bukkit.Location getPodium();

    @Unique
    public abstract void setPodium(org.bukkit.Location p0);
}
