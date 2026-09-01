package com.ixnah.mc.paperarc.mixin.common.api;

import java.util.Map;
import java.util.Optional;

import com.google.common.base.Preconditions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
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
 * <p>{@code recipesUsed} (f_58320_) and {@code cookingTotalTime} (f_58319_)
 * are vanilla fields widened via AT and accessed directly. The cook speed
 * multiplier is a Paper-patch member injected into the NMS block entity by
 * {@code AbstractFurnaceBlockEntityFieldsMixin} and reached through
 * {@link com.ixnah.mc.paperarc.bridge.AbstractFurnaceBlockEntityBridge}. The
 * protected {@code CraftBlockEntityState#getSnapshot()} is reached through the
 * merged {@link CraftBlockEntityStateBridge} (provider mixin on the base class)
 * instead of a subclass @Shadow.</p>
 */
@Mixin(CraftFurnace.class)
public abstract class CraftFurnaceApiMixin {

    @Unique
    private AbstractFurnaceBlockEntity paperarc$snapshot() {
        Object snapshot = ((CraftBlockEntityStateBridge) (Object) this).paperarc$getSnapshot();
        return snapshot instanceof AbstractFurnaceBlockEntity afbe ? afbe : null;
    }
    @Unique
    public double getCookSpeedMultiplier() {
        AbstractFurnaceBlockEntity snapshot = this.paperarc$snapshot();
        return snapshot == null ? 1.0D
                : ((AbstractFurnaceBlockEntityBridge) snapshot).paper$getCookSpeedMultiplier();
    }

    @Unique
    public void setCookSpeedMultiplier(double multiplier) {
        Preconditions.checkArgument(multiplier >= 0, "Furnace speed multiplier cannot be negative");
        Preconditions.checkArgument(multiplier <= 200, "Furnace speed multiplier cannot more than 200");
        AbstractFurnaceBlockEntity snapshot = this.paperarc$snapshot();
        if (snapshot != null) {
            ((AbstractFurnaceBlockEntityBridge) snapshot).paper$setCookSpeedMultiplier(multiplier);
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
        Optional<? extends net.minecraft.world.item.crafting.Recipe<?>> nmsRecipe = recipeManager.byKey(location);
        Preconditions.checkArgument(nmsRecipe.isPresent() && nmsRecipe.get() instanceof AbstractCookingRecipe,
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

    @SuppressWarnings("unchecked")
    @Unique
    private Map<ResourceLocation, Integer> paperarc$recipesUsed() {
        AbstractFurnaceBlockEntity snapshot = this.paperarc$snapshot();
        // recipesUsed 由 AT 加宽（f_58320_）后直访
        return (Map<ResourceLocation, Integer>) (Map<?, ?>) snapshot.recipesUsed;
    }

    @Unique
    private boolean paperarc$isPlaced() {
        return ((CraftBlockState) (Object) this).isPlaced();
    }
}
