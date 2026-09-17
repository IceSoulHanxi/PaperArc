package com.ixnah.mc.paperarc.bridge;

/**
 * Paper 在 {@code AnvilMenu} / {@code BrewingStandMenu} 上补充的纯 API 状态
 * （无 NMS 访问器），由 {@code mojmap/inventory/*FieldsMixin} 注入。
 * 两个菜单各自只有一个标量，合用一个 duck 接口即可。
 */
public interface MenuFieldsBridge {

    /** {@code AnvilMenu.bypassEnchantmentLevelRestriction}。 */
    boolean paper$bypassEnchantmentLevelRestriction();

    void paper$setBypassEnchantmentLevelRestriction(boolean bypass);

    /** {@code BrewingStandMenu.recipeBrewTime}（vanilla 默认 400 tick）。 */
    int paper$getRecipeBrewTime();

    void paper$setRecipeBrewTime(int recipeBrewTime);
}
