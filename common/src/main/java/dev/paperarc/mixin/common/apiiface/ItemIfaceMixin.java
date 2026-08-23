package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Item} (generated).
 * Adds 8 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Item", remap = false)
public interface ItemIfaceMixin {

    public abstract boolean canMobPickup();

    public abstract void setCanMobPickup(boolean p0);

    public abstract boolean canPlayerPickup();

    public abstract void setCanPlayerPickup(boolean p0);

    public abstract boolean willAge();

    public abstract void setWillAge(boolean p0);

    public abstract int getHealth();

    public abstract void setHealth(int p0);
}
