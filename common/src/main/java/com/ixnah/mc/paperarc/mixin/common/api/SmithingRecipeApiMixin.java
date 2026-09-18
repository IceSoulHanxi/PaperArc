package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.inventory.SmithingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code SmithingRecipe#willCopyNbt()}（A6/X-2 第五批）。
 *
 * <p>paper 把 {@code copyNbt} 做成构造参数（只有 {@code SmithingTransformRecipe} 等
 * 才可能为 false）；1.20.1 的 vanilla 锻造台**总是**保留原物品的 NBT，运行时也没有
 * 这个开关，所以恒返回 true —— 与 1.20.1 的实际行为一致，不是占位。
 */
@Mixin(SmithingRecipe.class)
public abstract class SmithingRecipeApiMixin {

    @Unique
    public boolean willCopyNbt() {
        return true;
    }
}
