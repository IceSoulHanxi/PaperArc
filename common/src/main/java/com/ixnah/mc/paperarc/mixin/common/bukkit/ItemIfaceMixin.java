package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Item} (generated).
 * Adds 8 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Item", remap = false)
public interface ItemIfaceMixin {

    @Unique
    public abstract boolean canMobPickup();

    @Unique
    public abstract void setCanMobPickup(boolean p0);

    @Unique
    public abstract boolean canPlayerPickup();

    @Unique
    public abstract void setCanPlayerPickup(boolean p0);

    @Unique
    public abstract boolean willAge();

    @Unique
    public abstract void setWillAge(boolean p0);

    @Unique
    public abstract int getHealth();

    @Unique
    public abstract void setHealth(int p0);
}
