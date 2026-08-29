package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.AbstractArrow} (generated, trimmed for 1.20.1).
 * Adds 5 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.AbstractArrow", remap = false)
public interface AbstractArrowIfaceMixin {

    @Unique
    public abstract org.bukkit.inventory.ItemStack getItemStack();

    @Unique
    public abstract void setLifetimeTicks(int p0);

    @Unique
    public abstract int getLifetimeTicks();

    @Unique
    public abstract org.bukkit.Sound getHitSound();

    @Unique
    public abstract void setHitSound(org.bukkit.Sound p0);
    @Unique
    public abstract void setNoPhysics(boolean noPhysics);
    @Unique
    public abstract boolean hasNoPhysics();
}
