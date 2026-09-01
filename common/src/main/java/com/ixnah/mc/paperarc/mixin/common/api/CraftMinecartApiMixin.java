package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v.entity.CraftMinecart;
import org.bukkit.craftbukkit.v.util.CraftMagicNumbers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

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
        Item minecartItem = switch (this.getHandle().getMinecartType()) {
            case CHEST -> Items.CHEST_MINECART;
            case FURNACE -> Items.FURNACE_MINECART;
            case TNT -> Items.TNT_MINECART;
            case HOPPER -> Items.HOPPER_MINECART;
            case COMMAND_BLOCK -> Items.COMMAND_BLOCK_MINECART;
            case RIDEABLE, SPAWNER -> Items.MINECART;
        };
        return CraftMagicNumbers.getMaterial(minecartItem);
    }
}
