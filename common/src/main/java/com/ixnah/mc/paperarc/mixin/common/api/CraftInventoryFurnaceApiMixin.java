package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.AbstractFurnaceBlockEntityBridge;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryFurnace;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's {@code Expose-isFuel-and-canSmelt-methods-to-FurnaceInvento.patch}
 * to {@link CraftInventoryFurnace}: {@code isFuel(ItemStack)} and
 * {@code canSmelt(ItemStack)}.
 *
 * <p>Both delegate to the NMS furnace recipe machinery; the {@code inventory}
 * field of the {@code CraftInventory} base is shadowed to reach the
 * {@code AbstractFurnaceBlockEntity}. The furnace {@code recipeType} is private,
 * exposed through {@link AbstractFurnaceBlockEntityBridge}.</p>
 */
@Mixin(CraftInventoryFurnace.class)
public abstract class CraftInventoryFurnaceApiMixin {

    @Shadow
    protected final net.minecraft.world.Container inventory = null;

    @Unique
    public boolean isFuel(ItemStack stack) {
        return stack != null && !stack.getType().isEmpty()
                && AbstractFurnaceBlockEntity.isFuel(CraftItemStack.asNMSCopy(stack));
    }

    @Unique
    public boolean canSmelt(ItemStack stack) {
        // data packs are always loaded in the main world
        net.minecraft.server.level.ServerLevel world =
                ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
        return stack != null && !stack.getType().isEmpty()
                && world.getRecipeManager().getRecipeFor(
                        ((AbstractFurnaceBlockEntityBridge) this.inventory).paper$getRecipeType(),
                        new SimpleContainer(CraftItemStack.asNMSCopy(stack)), world).isPresent();
    }
}
