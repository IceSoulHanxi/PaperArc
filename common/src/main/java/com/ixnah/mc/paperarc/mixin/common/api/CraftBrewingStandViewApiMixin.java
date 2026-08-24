package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.inventory.BrewingStandMenu;
import org.bukkit.craftbukkit.v.inventory.view.CraftBrewingStandView;
import com.ixnah.mc.paperarc.bridge.craft.CraftInventoryViewBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's BrewingStandView recipe-brew-time API.
 *
 * Paper stores this as a third value of the brewing stand ContainerData plus a
 * new BrewingStandBlockEntity.recipeBrewTime field; vanilla 1.21.1 NMS has
 * neither (its data delegate only carries indices 0/1), so the state is kept in
 * the ApiState side map keyed by the menu. Default 400 matches the vanilla
 * constant; the actual brewing countdown is NOT governed by this value.
 */
@Mixin(CraftBrewingStandView.class)
public abstract class CraftBrewingStandViewApiMixin {

    @Unique
    private static final String PAPERARC$RECIPE_BREW_TIME = "paperarc:recipeBrewTime";

    @Unique
    private BrewingStandMenu paperarc$menu() {
        return (BrewingStandMenu) ((CraftInventoryViewBridge) (Object) this).paperarc$menu();
    }

    @Unique
    public int getRecipeBrewTime() {
        return com.ixnah.mc.paperarc.bridge.ApiState.get(paperarc$menu(), PAPERARC$RECIPE_BREW_TIME, 400);
    }

    @Unique
    public void setRecipeBrewTime(int recipeBrewTime) {
        com.google.common.base.Preconditions.checkArgument(recipeBrewTime > 0, "recipeBrewTime must be positive");
        com.ixnah.mc.paperarc.bridge.ApiState.put(paperarc$menu(), PAPERARC$RECIPE_BREW_TIME, recipeBrewTime);
    }
}
