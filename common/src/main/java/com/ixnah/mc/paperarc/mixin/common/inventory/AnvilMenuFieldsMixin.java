package com.ixnah.mc.paperarc.mixin.common.inventory;

import com.ixnah.mc.paperarc.bridge.MenuFieldsBridge;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code AnvilMenu.bypassEnchantmentLevelRestriction}.
 *
 * <p>注意：这里只提供**存取**。Paper 还在 {@code createResult} 里读这个标志来跳过
 * 等级上限校验，那一段 Arclight 未改，本项目也没注入 —— 所以该 API 目前只是
 * 忠实保存插件写入的值，附加效果没有生效（与改成 ApiState 之前的行为一致）。
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuFieldsMixin implements MenuFieldsBridge {

    @Unique
    public boolean bypassEnchantmentLevelRestriction; // Paper

    @Override
    public boolean paper$bypassEnchantmentLevelRestriction() {
        return this.bypassEnchantmentLevelRestriction;
    }

    @Override
    public void paper$setBypassEnchantmentLevelRestriction(boolean bypass) {
        this.bypassEnchantmentLevelRestriction = bypass;
    }

    @Override
    public int paper$getRecipeBrewTime() {
        throw new UnsupportedOperationException("recipeBrewTime 属于 BrewingStandMenu");
    }

    @Override
    public void paper$setRecipeBrewTime(int recipeBrewTime) {
        throw new UnsupportedOperationException("recipeBrewTime 属于 BrewingStandMenu");
    }
}
