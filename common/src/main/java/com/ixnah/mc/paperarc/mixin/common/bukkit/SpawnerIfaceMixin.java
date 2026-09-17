package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.spawner.Spawner}.
 * Adds paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).
 */
@Mixin(targets = "org.bukkit.spawner.Spawner", remap = false)
public interface SpawnerIfaceMixin {

    @Unique
    public abstract boolean isActivated();

    @Unique
    public abstract void resetTimer();

    @Unique
    public abstract void setSpawnedItem(org.bukkit.inventory.ItemStack p0);
}
