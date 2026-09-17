package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.loot.Lootable}.
 * Adds paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).
 */
@Mixin(targets = "org.bukkit.loot.Lootable", remap = false)
public interface LootableIfaceMixin {

    @Unique
    public abstract void setLootTable(org.bukkit.loot.LootTable p0, long p1);
}
