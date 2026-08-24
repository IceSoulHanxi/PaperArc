package dev.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Firework} (generated).
 * Adds 7 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Firework", remap = false)
public interface FireworkIfaceMixin {

    @Unique
    public abstract java.util.UUID getSpawningEntity();

    @Unique
    public abstract org.bukkit.inventory.ItemStack getItem();

    @Unique
    public abstract void setItem(org.bukkit.inventory.ItemStack p0);

    @Unique
    public abstract int getTicksFlown();

    @Unique
    public abstract void setTicksFlown(int p0);

    @Unique
    public abstract int getTicksToDetonate();

    @Unique
    public abstract void setTicksToDetonate(int p0);
}
