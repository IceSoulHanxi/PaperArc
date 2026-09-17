package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.AbstractFurnaceBlockEntityBridge;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import org.bukkit.Bukkit;
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
        return stack != null && stack.getType() != org.bukkit.Material.AIR
                && AbstractFurnaceBlockEntity.isFuel(CraftItemStack.asNMSCopy(stack));
    }

    @Unique
    public boolean canSmelt(ItemStack stack) {
        // data packs are always loaded in the main world
        net.minecraft.server.level.ServerLevel world =
                ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
        return stack != null && stack.getType() != org.bukkit.Material.AIR
                && world.getRecipeManager().getRecipeFor(
                        paperarc$recipeType(),
                        // 1.21.1 的 getRecipeFor 收的是 RecipeInput 而不是 Container
                        new net.minecraft.world.item.crafting.SingleRecipeInput(
                                CraftItemStack.asNMSCopy(stack)), world).isPresent();
    }

    /**
     * 炉子的配方类型。**不走 @Shadow**：{@code AbstractFurnaceBlockEntity.recipeType} 是
     * private final 泛型字段，注解处理器不给它生成 refmap 条目，Fabric 上运行期报
     * "@Shadow field recipeType was not located"（B6 实测）。三种炉子按具体类型分发即可，
     * 与 vanilla 各自构造器里传的值一致。
     */
    @Unique
    private net.minecraft.world.item.crafting.RecipeType<? extends net.minecraft.world.item.crafting.AbstractCookingRecipe>
            paperarc$recipeType() {
        Object container = this.paperarc$container();
        if (container instanceof net.minecraft.world.level.block.entity.SmokerBlockEntity) {
            return net.minecraft.world.item.crafting.RecipeType.SMOKING;
        }
        if (container instanceof net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity) {
            return net.minecraft.world.item.crafting.RecipeType.BLASTING;
        }
        return net.minecraft.world.item.crafting.RecipeType.SMELTING;
    }
}
