package com.ixnah.mc.paperarc.mixin.common.api;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.EnchantmentMenu;

/**
 * Exposes private EnchantmentMenu#enchantmentSeed for CraftEnchantmentViewApiMixin#setEnchantmentSeed.
 * Paper ref: patches/server/Add-enchantment-seed-update-API.patch.
 */
@Mixin(EnchantmentMenu.class)
public interface EnchantmentMenuAccessorMixin {

    @Accessor("enchantmentSeed")
    DataSlot paperarc$getEnchantmentSeedSlot();
}
