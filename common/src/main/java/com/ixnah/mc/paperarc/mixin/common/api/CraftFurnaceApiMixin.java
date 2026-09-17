package com.ixnah.mc.paperarc.mixin.common.api;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import com.google.common.base.Preconditions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import com.ixnah.mc.paperarc.bridge.AbstractFurnaceBlockEntityBridge;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.block.CraftBlockState;
import org.bukkit.craftbukkit.v.block.CraftFurnace;
import org.bukkit.craftbukkit.v.util.CraftNamespacedKey;
import org.bukkit.inventory.CookingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Ports of Paper's Implement-furnace-cook-speed-multiplier-API.patch and
 * Furnace-RecipesUsed-API.patch additions on {@link CraftFurnace}.
 *
 * <p>Paper stores the cook speed multiplier in a public double field
 * {@code cookSpeedMultiplier} added to {@link AbstractFurnaceBlockEntity}; that
 * field is injected by {@code AbstractFurnaceBlockEntityFieldsMixin} and read
 * through {@link AbstractFurnaceBlockEntityBridge}. The used-recipe counts come
 * from the vanilla private {@code recipesUsed} map, opened (together with
 * {@code cookingTotalTime} and {@code getTotalCookTime}) by
 * {@code paperarc.accesswidener}. {@code CraftBlockEntityState#getSnapshot()}
 * is protected and reached through {@link CraftBlockEntityStateBridge}.
 */
@Mixin(CraftFurnace.class)
public abstract class CraftFurnaceApiMixin {

// Paper's 4-arg (Level, RecipeType, AFBE, double) overload

    // Paper start - cook speed multiplier API

    @Unique
    public double getCookSpeedMultiplier() {
        AbstractFurnaceBlockEntity snapshot = this.paperarc$snapshot();
        if (snapshot == null) {
            return 1.0D;
        }
        return ((AbstractFurnaceBlockEntityBridge) snapshot).paper$getCookSpeedMultiplier();
    }

    @Unique
    public void setCookSpeedMultiplier(double multiplier) {
        Preconditions.checkArgument(multiplier >= 0, "Furnace speed multiplier cannot be negative");
        Preconditions.checkArgument(multiplier <= 200, "Furnace speed multiplier cannot more than 200");
        AbstractFurnaceBlockEntity snapshot = this.paperarc$snapshot();
        if (snapshot == null) {
            return;
        }
        ((AbstractFurnaceBlockEntityBridge) snapshot).paper$setCookSpeedMultiplier(multiplier);
        // Paper: rescale the snapshot's current total cook time to the new multiplier.
        // Paper 的 4 参 getTotalCookTime(Level, RecipeType, AFBE, double) 是它自己加的，
        // vanilla 只有 2 参版本，这里按同样的语义除以倍率。
        Level level = this.paperarc$isPlaced() ? (Level) ((CraftBlockState) (Object) this).getWorldHandle() : null;
        if (level != null) {
            int vanillaTotal = AbstractFurnaceBlockEntity.getTotalCookTime(level, snapshot);
            snapshot.cookingTotalTime = multiplier <= 0.0D
                    ? vanillaTotal
                    : (int) Math.ceil(vanillaTotal / multiplier);
        }
    }

    // Paper start - Furnace RecipesUsed API

    @Unique
    public int getRecipeUsedCount(NamespacedKey furnaceRecipe) {
        Map<ResourceLocation, Integer> recipesUsed = this.paperarc$recipesUsed();
        Integer count = recipesUsed.get(CraftNamespacedKey.toMinecraft(furnaceRecipe));
        return count != null ? count : 0;
    }

    @Unique
    public boolean hasRecipeUsedCount(NamespacedKey furnaceRecipe) {
        return this.paperarc$recipesUsed().containsKey(CraftNamespacedKey.toMinecraft(furnaceRecipe));
    }

    @Unique
    public void setRecipeUsedCount(CookingRecipe<?> furnaceRecipe, int count) {
        ResourceLocation location = CraftNamespacedKey.toMinecraft(furnaceRecipe.getKey());
        Level level = this.paperarc$isPlaced() ? (Level) ((CraftBlockState) (Object) this).getWorldHandle() : null;
        // this mapping has no MinecraftServer.getServer(); reach it via CraftServer
        RecipeManager recipeManager = level != null ? level.getRecipeManager()
            : ((CraftServer) PaperArcBridge.getServer()).getServer().getRecipeManager();
        Optional<RecipeHolder<?>> nmsRecipe = recipeManager.byKey(location);
        Preconditions.checkArgument(nmsRecipe.isPresent() && nmsRecipe.get().value() instanceof AbstractCookingRecipe,
            furnaceRecipe.getKey() + " is not recognized as a valid and registered furnace recipe");
        if (count > 0) {
            this.paperarc$recipesUsed().put(location, count);
        } else {
            this.paperarc$recipesUsed().remove(location);
        }
    }

    @Unique
    public void setRecipesUsed(Map<CookingRecipe<?>, Integer> recipesUsed) {
        this.paperarc$recipesUsed().clear();
        recipesUsed.forEach((recipe, integer) -> {
            if (integer != null) {
                this.setRecipeUsedCount(recipe, integer);
            }
        });
    }

    @Unique
    private Map<ResourceLocation, Integer> paperarc$recipesUsed() {
        return this.paperarc$snapshot().recipesUsed;
    }

    @Unique
    private AbstractFurnaceBlockEntity paperarc$snapshot() {
        return (AbstractFurnaceBlockEntity) ((CraftBlockEntityStateBridge) (Object) this).paperarc$getSnapshot();
    }

    @Unique
    private boolean paperarc$isPlaced() {
        return ((CraftBlockState) (Object) this).isPlaced();
    }

}
