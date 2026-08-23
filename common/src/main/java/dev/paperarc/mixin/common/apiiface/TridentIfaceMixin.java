package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Trident} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Trident", remap = false)
public interface TridentIfaceMixin {

    public abstract boolean hasGlint();

    public abstract void setGlint(boolean p0);

    public abstract int getLoyaltyLevel();

    public abstract void setLoyaltyLevel(int p0);

    public abstract boolean hasDealtDamage();

    public abstract void setHasDealtDamage(boolean p0);
}
