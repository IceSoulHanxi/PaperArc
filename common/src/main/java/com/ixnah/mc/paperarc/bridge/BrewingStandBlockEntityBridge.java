package com.ixnah.mc.paperarc.bridge;

/**
 * Duck interface exposing Paper's {@code BrewingStandBlockEntity.recipeBrewTime}
 * supplementary field to the api mixins (Add-recipeBrewTime.patch). Paper's patch
 * adds the field without an NMS accessor, so the bridge methods carry the
 * {@code paper$} prefix.
 */
public interface BrewingStandBlockEntityBridge {

    int paper$getRecipeBrewTime();

    void paper$setRecipeBrewTime(int recipeBrewTime);
}
