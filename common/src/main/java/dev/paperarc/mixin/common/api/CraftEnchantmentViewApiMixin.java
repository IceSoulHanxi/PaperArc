package dev.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.inventory.view.CraftEnchantmentView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.EnchantmentMenu;

/**
 * Adds setEnchantmentSeed missing from Arclight CraftBukkit.
 * Paper ref: patches/server/Add-enchantment-seed-update-API.patch.
 */
@Mixin(CraftEnchantmentView.class)
public abstract class CraftEnchantmentViewApiMixin {

    @Shadow
    public abstract AbstractContainerMenu getHandle();

    @Unique
    public void setEnchantmentSeed(int seed) {
        ((EnchantmentMenuAccessorMixin) (EnchantmentMenu) this.getHandle()).paperarc$getEnchantmentSeedSlot().set(seed);
    }
}
