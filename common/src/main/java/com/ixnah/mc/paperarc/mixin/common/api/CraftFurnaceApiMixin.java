package com.ixnah.mc.paperarc.mixin.common.api;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

import com.google.common.base.Preconditions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
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
 * {@code cookSpeedMultiplier} added to {@link AbstractFurnaceBlockEntity} and
 * exposes the used-recipe counts through a Paper-added
 * {@code getRecipesUsed()} accessor; neither exists in the vanilla mojmap
 * compile jar, so they are accessed via reflection.
 * {@code CraftBlockEntityState#getSnapshot()} is protected (subclass-target
 * mixins cannot shadow it), hence reflection there too.
 */
@Mixin(CraftFurnace.class)
public abstract class CraftFurnaceApiMixin {

    @Unique
    private static final String PAPERARC$SNAPSHOT_OWNER = "org.bukkit.craftbukkit.v.block.CraftBlockEntityState";

    @Unique
    private static Method paperarc$snapshotMethod;

    @Unique
    private static Field paperarc$cookSpeedMultiplierField;

    @Unique
    private static Field paperarc$recipeTypeField;

    @Unique
    private static Field paperarc$cookingTotalTimeField;

    @Unique
    private static Method paperarc$getTotalCookTimeMethod; // Paper's 4-arg (Level, RecipeType, AFBE, double) overload

    @Unique
    private static Method paperarc$recipesUsedMethod;

    @Unique
    private static Field paperarc$recipesUsedField;

    // Paper start - cook speed multiplier API

    @Unique
    public double getCookSpeedMultiplier() {
        AbstractFurnaceBlockEntity snapshot = this.paperarc$snapshot();
        if (snapshot == null) {
            return 1.0D;
        }
        try {
            return paperarc$cookSpeedMultiplierField().getDouble(snapshot);
        } catch (ReflectiveOperationException e) {
            return 1.0D; // Paper/vanilla default multiplier
        }
    }

    @Unique
    public void setCookSpeedMultiplier(double multiplier) {
        Preconditions.checkArgument(multiplier >= 0, "Furnace speed multiplier cannot be negative");
        Preconditions.checkArgument(multiplier <= 200, "Furnace speed multiplier cannot more than 200");
        AbstractFurnaceBlockEntity snapshot = this.paperarc$snapshot();
        if (snapshot == null) {
            return;
        }
        try {
            paperarc$cookSpeedMultiplierField().setDouble(snapshot, multiplier);
            // Paper: rescale the snapshot's current total cook time to the new multiplier
            Method totalCookTime = paperarc$getTotalCookTimeMethod();
            if (totalCookTime != null) {
                Level level = this.paperarc$isPlaced() ? (Level) ((CraftBlockState) (Object) this).getWorldHandle() : null;
                paperarc$cookingTotalTimeField().setInt(snapshot,
                    (Integer) totalCookTime.invoke(null, level, paperarc$recipeTypeField().get(snapshot), snapshot, multiplier));
            }
        } catch (ReflectiveOperationException e) {
            // unpatched runtime: vanilla keeps multiplier 1.0
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
        try {
            if (paperarc$recipesUsedMethod == null && paperarc$recipesUsedField == null) {
                try {
                    paperarc$recipesUsedMethod = AbstractFurnaceBlockEntity.class.getDeclaredMethod("getRecipesUsed"); // Paper-added accessor
                    paperarc$recipesUsedMethod.setAccessible(true);
                } catch (NoSuchMethodException e) {
                    paperarc$recipesUsedField = AbstractFurnaceBlockEntity.class.getDeclaredField("recipesUsed"); // vanilla map field
                    paperarc$recipesUsedField.setAccessible(true);
                }
            }
            Object map = paperarc$recipesUsedMethod != null
                ? paperarc$recipesUsedMethod.invoke(snapshot)
                : paperarc$recipesUsedField.get(snapshot);
            return (Map<ResourceLocation, Integer>) map;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("PaperArc: cannot access AbstractFurnaceBlockEntity used-recipes map", e);
        }
    }

    @Unique
    private AbstractFurnaceBlockEntity paperarc$snapshot() {
        try {
            if (paperarc$snapshotMethod == null) {
                Method method = Class.forName(PAPERARC$SNAPSHOT_OWNER).getDeclaredMethod("getSnapshot");
                method.setAccessible(true);
                paperarc$snapshotMethod = method;
            }
            return (AbstractFurnaceBlockEntity) paperarc$snapshotMethod.invoke(this);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("PaperArc: cannot access CraftBlockEntityState#getSnapshot()", e);
        }
    }

    @Unique
    private boolean paperarc$isPlaced() {
        return ((CraftBlockState) (Object) this).isPlaced();
    }

    @Unique
    private static Field paperarc$cookSpeedMultiplierField() throws NoSuchFieldException {
        if (paperarc$cookSpeedMultiplierField == null) {
            Field field = AbstractFurnaceBlockEntity.class.getField("cookSpeedMultiplier"); // public in Paper's runtime patch
            field.setAccessible(true);
            paperarc$cookSpeedMultiplierField = field;
        }
        return paperarc$cookSpeedMultiplierField;
    }

    @Unique
    private static Field paperarc$recipeTypeField() throws NoSuchFieldException {
        if (paperarc$recipeTypeField == null) {
            Field field = AbstractFurnaceBlockEntity.class.getDeclaredField("recipeType");
            field.setAccessible(true);
            paperarc$recipeTypeField = field;
        }
        return paperarc$recipeTypeField;
    }

    @Unique
    private static Field paperarc$cookingTotalTimeField() throws NoSuchFieldException {
        if (paperarc$cookingTotalTimeField == null) {
            Field field = AbstractFurnaceBlockEntity.class.getDeclaredField("cookingTotalTime");
            field.setAccessible(true);
            paperarc$cookingTotalTimeField = field;
        }
        return paperarc$cookingTotalTimeField;
    }

    @Unique
    private static Method paperarc$getTotalCookTimeMethod() {
        if (paperarc$getTotalCookTimeMethod == null) {
            for (Method method : AbstractFurnaceBlockEntity.class.getDeclaredMethods()) {
                Class<?>[] params = method.getParameterTypes();
                // Paper's overload: (Level, RecipeType, AbstractFurnaceBlockEntity, double)
                if (method.getName().equals("getTotalCookTime") && params.length == 4 && params[3] == double.class) {
                    method.setAccessible(true);
                    paperarc$getTotalCookTimeMethod = method;
                    break;
                }
            }
        }
        return paperarc$getTotalCookTimeMethod;
    }
}
