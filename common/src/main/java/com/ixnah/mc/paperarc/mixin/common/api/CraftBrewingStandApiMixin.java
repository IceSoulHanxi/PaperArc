package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import com.ixnah.mc.paperarc.bridge.BrewingStandBlockEntityBridge;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;

import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.bukkit.craftbukkit.v.block.CraftBrewingStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's Add-recipeBrewTime.patch additions on {@link CraftBrewingStand}:
 * {@code BrewingStand#getRecipeBrewTime()} / {@code setRecipeBrewTime(int)}.
 *
 * <p>Paper's implementation reads/writes the public int field
 * {@code recipeBrewTime} (default 400) which it adds to
 * {@link BrewingStandBlockEntity}; that field does not exist in vanilla nor in
 * Arclight, so {@code BrewingStandBlockEntityFieldsMixin} injects it and this
 * mixin reaches it through {@link BrewingStandBlockEntityBridge}.
 * {@code CraftBlockEntityState#getSnapshot()} is protected and is reached
 * through {@link CraftBlockEntityStateBridge}.
 */
@Mixin(CraftBrewingStand.class)
public abstract class CraftBrewingStandApiMixin {

    @Unique
    public int getRecipeBrewTime() {
        BrewingStandBlockEntity snapshot = this.paperarc$snapshot();
        if (snapshot == null) {
            return 400; // Paper/vanilla default recipe brew time
        }
        return ((BrewingStandBlockEntityBridge) snapshot).paper$getRecipeBrewTime();
    }

    @Unique
    public void setRecipeBrewTime(int recipeBrewTime) {
        Preconditions.checkArgument(recipeBrewTime > 0, "recipeBrewTime must be positive");
        BrewingStandBlockEntity snapshot = this.paperarc$snapshot();
        if (snapshot == null) {
            return;
        }
        ((BrewingStandBlockEntityBridge) snapshot).paper$setRecipeBrewTime(recipeBrewTime);
    }

    @Unique
    private BrewingStandBlockEntity paperarc$snapshot() {
        return (BrewingStandBlockEntity) ((CraftBlockEntityStateBridge) (Object) this).paperarc$getSnapshot();
    }

}
