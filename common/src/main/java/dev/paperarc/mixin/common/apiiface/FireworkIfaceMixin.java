package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Firework} (generated).
 * Adds 7 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Firework", remap = false)
public interface FireworkIfaceMixin {

    public abstract java.util.UUID getSpawningEntity();

    public abstract org.bukkit.inventory.ItemStack getItem();

    public abstract void setItem(org.bukkit.inventory.ItemStack p0);

    public abstract int getTicksFlown();

    public abstract void setTicksFlown(int p0);

    public abstract int getTicksToDetonate();

    public abstract void setTicksToDetonate(int p0);
}
