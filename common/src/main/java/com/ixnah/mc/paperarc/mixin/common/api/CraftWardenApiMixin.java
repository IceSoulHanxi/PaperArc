package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.entity.CraftWarden;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.entity.monster.warden.Warden;

/**
 * Adds getHighestAnger missing from Arclight CraftBukkit.
 * Paper ref: patches/server/Missing-Entity-API.patch
 * (getHandle().getAngerManagement().getActiveAnger(null)).
 */
@Mixin(CraftWarden.class)
public abstract class CraftWardenApiMixin {

    @Shadow
    public abstract Warden getHandle();

    @Unique
    public int getHighestAnger() {
        return this.getHandle().getAngerManagement().getActiveAnger(null);
    }
}
