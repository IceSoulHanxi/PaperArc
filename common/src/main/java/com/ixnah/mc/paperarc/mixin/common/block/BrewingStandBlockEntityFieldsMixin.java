package com.ixnah.mc.paperarc.mixin.common.block;

import com.ixnah.mc.paperarc.bridge.BrewingStandBlockEntityBridge;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code BrewingStandBlockEntity.recipeBrewTime} supplementary
 * field (Add-recipeBrewTime.patch). Field name matches Paper exactly (no
 * {@code paperarc$} prefix) for reflection ABI compatibility; access methods
 * carry the {@code paper$} prefix through {@link BrewingStandBlockEntityBridge}
 * because Paper's patch adds no NMS accessor.
 */
@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityFieldsMixin implements BrewingStandBlockEntityBridge {

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
}
