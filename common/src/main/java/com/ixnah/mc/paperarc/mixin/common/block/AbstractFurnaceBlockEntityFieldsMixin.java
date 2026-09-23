package com.ixnah.mc.paperarc.mixin.common.block;

import com.ixnah.mc.paperarc.bridge.AbstractFurnaceBlockEntityBridge;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

/**
 * Injects Paper's {@code AbstractFurnaceBlockEntity.cookSpeedMultiplier}
 * supplementary field (Implement-furnace-cook-speed-multiplier-API.patch). Field
 * name matches Paper exactly (no {@code paperarc$} prefix) for reflection ABI
 * compatibility; access methods carry the {@code paper$} prefix through
 * {@link AbstractFurnaceBlockEntityBridge} because Paper's patch adds no NMS
 * accessor.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityFieldsMixin implements AbstractFurnaceBlockEntityBridge {

    @Unique
    public double cookSpeedMultiplier = 1.0D; // Paper

    @Override
    public double paper$getCookSpeedMultiplier() {
        return this.cookSpeedMultiplier;
    }

    @Override
    public void paper$setCookSpeedMultiplier(double multiplier) {
        this.cookSpeedMultiplier = multiplier;
    }

    @Shadow
    private it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed;

    @Override
    public Map<ResourceKey<Recipe<?>>, Integer> paper$getRecipesUsed() {
        return this.recipesUsed;
    }

}
