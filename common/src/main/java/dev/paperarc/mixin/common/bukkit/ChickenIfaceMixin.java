package dev.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Chicken} (generated).
 * Adds 4 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Chicken", remap = false)
public interface ChickenIfaceMixin {

    @Unique
    public abstract boolean isChickenJockey();

    @Unique
    public abstract void setIsChickenJockey(boolean p0);

    @Unique
    public abstract int getEggLayTime();

    @Unique
    public abstract void setEggLayTime(int p0);
}
