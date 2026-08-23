package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.AbstractArrow} (generated).
 * Adds 7 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.AbstractArrow", remap = false)
public interface AbstractArrowIfaceMixin {

    @Unique
    public abstract org.bukkit.inventory.ItemStack getItemStack();

    @Unique
    public abstract void setItemStack(org.bukkit.inventory.ItemStack p0);

    @Unique
    public abstract void setLifetimeTicks(int p0);

    @Unique
    public abstract int getLifetimeTicks();

    @Unique
    public abstract org.bukkit.Sound getHitSound();

    @Unique
    public abstract void setHitSound(org.bukkit.Sound p0);

    @Unique
    public abstract void setShooter(org.bukkit.projectiles.ProjectileSource p0, boolean p1);
}
