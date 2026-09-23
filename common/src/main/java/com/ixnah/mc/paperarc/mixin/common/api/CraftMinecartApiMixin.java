package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v.entity.CraftMinecart;
import org.bukkit.craftbukkit.v.util.CraftMagicNumbers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;

/**
 * Adds getMinecartMaterial missing from Arclight CraftBukkit.
 * Paper ref: patches/server/API-to-get-Material-from-Boats-and-Minecarts.patch.
 */
@Mixin(CraftMinecart.class)
public abstract class CraftMinecartApiMixin {

    @Shadow
    public abstract AbstractMinecart getHandle();

    @Unique
    public Material getMinecartMaterial() {
        ItemStack pickResult = this.getHandle().getPickResult();
        return pickResult != null ? CraftMagicNumbers.getMaterial(pickResult.getItem()) : Material.MINECART;
    }
}
