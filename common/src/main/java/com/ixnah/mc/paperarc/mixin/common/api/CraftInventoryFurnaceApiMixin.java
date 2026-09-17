package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.AbstractFurnaceBlockEntityBridge;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.bukkit.craftbukkit.v.inventory.CraftInventory;
import org.bukkit.craftbukkit.v.inventory.CraftInventoryFurnace;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's {@code Expose-isFuel-and-canSmelt-methods-to-FurnaceInvento.patch}
 * to {@link CraftInventoryFurnace}: {@code isFuel(ItemStack)} and
 * {@code canSmelt(ItemStack)}.
 *
 * <p>Both delegate to the NMS furnace recipe machinery. The backing
 * {@code AbstractFurnaceBlockEntity} is reached through the <b>public</b>
 * {@code CraftInventory#getInventory()} accessor rather than {@code @Shadow}-ing the
 * inherited {@code CraftInventory.inventory} field: shadowing a superclass member from a
 * subclass mixin is unreliable ("was not located in the target class", run35). The furnace
 * {@code recipeType} is private, exposed through {@link AbstractFurnaceBlockEntityBridge}.</p>
 */
@Mixin(CraftInventoryFurnace.class)
public abstract class CraftInventoryFurnaceApiMixin {

    @Unique
    private net.minecraft.world.Container paperarc$container() {
        return ((CraftInventory) (Object) this).getInventory();
    }

    @Unique
    public boolean isFuel(ItemStack stack) {
        return stack != null && stack.getType() != Material.AIR
                && AbstractFurnaceBlockEntity.isFuel(CraftItemStack.asNMSCopy(stack));
    }

    @Unique
    public boolean canSmelt(ItemStack stack) {
        // data packs are always loaded in the main world
        net.minecraft.server.level.ServerLevel world =
                ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
        return stack != null && stack.getType() != Material.AIR
                && world.getRecipeManager().getRecipeFor(
                        ((AbstractFurnaceBlockEntityBridge) this.paperarc$container()).paper$getRecipeType(),
                        new SimpleContainer(CraftItemStack.asNMSCopy(stack)), world).isPresent();
    }
}
