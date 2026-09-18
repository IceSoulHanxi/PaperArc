package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.inventory.MerchantRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code MerchantRecipe#setIgnoreDiscounts/shouldIgnoreDiscounts}（A6/X-2 第五批）。
 *
 * <p>状态存在 API 对象自己身上（{@code MerchantRecipe} 是插件 new 出来的普通对象，
 * 不存在包装对象身份问题）。**但 Arclight 把 MerchantRecipe 转成 NMS
 * {@code MerchantOffer} 时不认这个标志**（那是 Paper 给 MerchantOffer 加的字段），
 * 所以设为 true 不会真的免掉声望折扣 —— 语义差异记在 docs/gaps.md。
 */
@Mixin(MerchantRecipe.class)
public abstract class MerchantRecipeApiMixin {

    @Unique
    private boolean paperarc$ignoreDiscounts;

    @Unique
    public boolean shouldIgnoreDiscounts() {
        return this.paperarc$ignoreDiscounts;
    }

    @Unique
    public void setIgnoreDiscounts(boolean ignoreDiscounts) {
        this.paperarc$ignoreDiscounts = ignoreDiscounts;
    }
}
