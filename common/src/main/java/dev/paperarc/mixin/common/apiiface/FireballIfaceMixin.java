package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Fireball} (generated).
 * Adds 2 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Fireball", remap = false)
public interface FireballIfaceMixin {

    @Unique
    public abstract void setPower(org.bukkit.util.Vector p0);

    @Unique
    public abstract org.bukkit.util.Vector getPower();
}
