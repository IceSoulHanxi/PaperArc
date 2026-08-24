package dev.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Projectile} (generated).
 * Adds 8 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Projectile", remap = false)
public interface ProjectileIfaceMixin {

    @Unique
    public abstract boolean hasLeftShooter();

    @Unique
    public abstract void setHasLeftShooter(boolean p0);

    @Unique
    public abstract boolean hasBeenShot();

    @Unique
    public abstract void setHasBeenShot(boolean p0);

    @Unique
    public abstract boolean canHitEntity(org.bukkit.entity.Entity p0);

    @Unique
    public abstract void hitEntity(org.bukkit.entity.Entity p0);

    @Unique
    public abstract void hitEntity(org.bukkit.entity.Entity p0, org.bukkit.util.Vector p1);

    @Unique
    public abstract java.util.UUID getOwnerUniqueId();
}
