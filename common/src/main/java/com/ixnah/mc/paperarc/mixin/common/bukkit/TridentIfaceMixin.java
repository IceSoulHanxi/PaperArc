package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Trident} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Trident", remap = false)
public interface TridentIfaceMixin {

    @Unique
    public abstract boolean hasGlint();

    @Unique
    public abstract void setGlint(boolean p0);

    @Unique
    public abstract int getLoyaltyLevel();

    @Unique
    public abstract void setLoyaltyLevel(int p0);

    @Unique
    public abstract boolean hasDealtDamage();

    @Unique
    public abstract void setHasDealtDamage(boolean p0);
}
