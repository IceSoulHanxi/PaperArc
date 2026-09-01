package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v.entity.CraftBoat;
import org.bukkit.craftbukkit.v.util.CraftMagicNumbers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.entity.vehicle.Boat;

/**
 * Adds getBoatMaterial missing from Arclight CraftBukkit.
 * Paper ref: patches/server/API-to-get-Material-from-Boats-and-Minecarts.patch
 * (CraftMagicNumbers.getMaterial(getHandle().getDropItem())).
 */
@Mixin(CraftBoat.class)
public abstract class CraftBoatApiMixin {

    @Shadow
    public abstract Boat getHandle();

    @Unique
    public Material getBoatMaterial() {
        return CraftMagicNumbers.getMaterial(this.getHandle().getDropItem());
    }
}
