package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.projectiles.ProjectileSource}.
 * Adds paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).
 */
@Mixin(targets = "org.bukkit.projectiles.ProjectileSource", remap = false)
public interface ProjectileSourceIfaceMixin {

    @Unique
    public abstract org.bukkit.entity.Projectile launchProjectile(java.lang.Class p0, org.bukkit.util.Vector p1, java.util.function.Consumer p2);
}
