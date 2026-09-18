package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code ShapedRecipe#setIngredient(char, ItemStack)}（A6/X-2 第五批）：
 * 按 paper 走 {@code RecipeChoice.ExactChoice}，这样 NBT 也参与匹配。
 */
@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeApiMixin {

    @Unique
    public ShapedRecipe setIngredient(char key, ItemStack item) {
        return ((ShapedRecipe) (Object) this).setIngredient(key, new RecipeChoice.ExactChoice(item));
    }
}
