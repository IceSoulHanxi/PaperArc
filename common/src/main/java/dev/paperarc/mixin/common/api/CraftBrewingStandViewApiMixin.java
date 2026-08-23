package dev.paperarc.mixin.common.api;

import net.minecraft.world.inventory.BrewingStandMenu;
import org.bukkit.craftbukkit.v.inventory.view.CraftBrewingStandView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

    @Shadow
    @Final
    private BrewingStandMenu container;

    @Unique
    public int getRecipeBrewTime() {
        return dev.paperarc.bridge.ApiState.get(container, PAPERARC$RECIPE_BREW_TIME, 400);
    }

    @Unique
    public void setRecipeBrewTime(int recipeBrewTime) {
        com.google.common.base.Preconditions.checkArgument(recipeBrewTime > 0, "recipeBrewTime must be positive");
        dev.paperarc.bridge.ApiState.put(container, PAPERARC$RECIPE_BREW_TIME, recipeBrewTime);
    }
}
