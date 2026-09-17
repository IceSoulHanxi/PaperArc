package com.ixnah.mc.paperarc.mixin.common.inventory;

import com.ixnah.mc.paperarc.bridge.MenuFieldsBridge;
import net.minecraft.world.inventory.BrewingStandMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code BrewingStandMenu.recipeBrewTime}（vanilla 默认 400 tick）。
 *
 * <p>与 {@link AnvilMenuFieldsMixin} 同理，这里只提供存取；Paper 用它替换酿造耗时的
 * 那一段逻辑未注入，所以该 API 目前只忠实保存插件写入的值。
 */
@Mixin(BrewingStandMenu.class)
public abstract class BrewingStandMenuFieldsMixin implements MenuFieldsBridge {

    @Unique
    public int recipeBrewTime = 400; // Paper

    @Override
    public int paper$getRecipeBrewTime() {
        return this.recipeBrewTime;
    }

    @Override
    public void paper$setRecipeBrewTime(int recipeBrewTime) {
        this.recipeBrewTime = recipeBrewTime;
    }

    @Override
    public boolean paper$bypassEnchantmentLevelRestriction() {
        throw new UnsupportedOperationException("bypassEnchantmentLevelRestriction 属于 AnvilMenu");
    }

    @Override
    public void paper$setBypassEnchantmentLevelRestriction(boolean bypass) {
        throw new UnsupportedOperationException("bypassEnchantmentLevelRestriction 属于 AnvilMenu");
    }
}
