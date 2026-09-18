package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Iterator;
import java.util.List;

/**
 * paper 的 {@code ShapelessRecipe} 按 {@code ItemStack} 增删配料（A6/X-2 第五批）：
 * 逐行照抄 paper，走 {@code RecipeChoice.ExactChoice} 让 NBT 参与匹配。
 * {@code ingredients} 就声明在这个类上，{@code @Shadow} 直接拿。
 */
@Mixin(ShapelessRecipe.class)
public abstract class ShapelessRecipeApiMixin {

    @Shadow
    private List<RecipeChoice> ingredients;

    @Unique
    private ShapelessRecipe paperarc$self() {
        return (ShapelessRecipe) (Object) this;
    }

    @Unique
    public ShapelessRecipe addIngredient(ItemStack item) {
        return this.paperarc$self().addIngredient(item.getAmount(), item);
    }

    @Unique
    public ShapelessRecipe addIngredient(int count, ItemStack item) {
        Preconditions.checkArgument(this.ingredients.size() + count <= 9,
                "Shapeless recipes cannot have more than 9 ingredients");
        while (count-- > 0) {
            this.ingredients.add(new RecipeChoice.ExactChoice(item));
        }
        return this.paperarc$self();
    }

    @Unique
    public ShapelessRecipe removeIngredient(ItemStack item) {
        return this.paperarc$self().removeIngredient(1, item);
    }

    @Unique
    public ShapelessRecipe removeIngredient(int count, ItemStack item) {
        Iterator<RecipeChoice> iterator = this.ingredients.iterator();
        while (count > 0 && iterator.hasNext()) {
            RecipeChoice choice = iterator.next();
            if (choice.test(item)) {
                iterator.remove();
                count--;
            }
        }
        return this.paperarc$self();
    }
}
