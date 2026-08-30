package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.projectiles.ProjectileSource} (generated).
 * Adds 1 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.projectiles.ProjectileSource", remap = false)
public interface ProjectileSourceIfaceMixin {

    @Unique
    public abstract <T extends org.bukkit.entity.Projectile> T launchProjectile(java.lang.Class<? extends T> p0, org.bukkit.util.Vector p1, org.bukkit.util.Consumer<T> p2);

}