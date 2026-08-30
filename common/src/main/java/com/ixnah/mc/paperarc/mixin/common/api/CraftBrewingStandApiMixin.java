package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;

import com.ixnah.mc.paperarc.bridge.ApiState;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBrewingStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's Add-recipeBrewTime.patch additions on {@link CraftBrewingStand}:
 * {@code BrewingStand#getRecipeBrewTime()} / {@code setRecipeBrewTime(int)}.
 *
 * <p>Paper's implementation reads/writes the public int field
 * {@code recipeBrewTime} (default 400) which it adds to
 * {@link BrewingStandBlockEntity} — that field only exists in the patched
 * runtime jar, not in the vanilla mojmap NMS, so it is kept in the
 * {@link ApiState} side map keyed by the snapshot block entity (default 400).
 * {@code CraftBlockEntityState#getSnapshot()} is protected and reached via
 * @Shadow on the CraftBrewingStand host — no reflection.</p>
 */
@Mixin(CraftBrewingStand.class)
public abstract class CraftBrewingStandApiMixin {

    @Unique
    private static final String PAPERARC$RECIPE_BREW_TIME_KEY = "paperarc$recipeBrewTime";

    @Shadow
    protected abstract net.minecraft.world.level.block.entity.BlockEntity getSnapshot();

    @Unique
    public int getRecipeBrewTime() {
        BrewingStandBlockEntity snapshot = this.paperarc$snapshot();
        if (snapshot == null) {
            return 400; // Paper/vanilla default recipe brew time
        }
        Integer stored = ApiState.get(snapshot, PAPERARC$RECIPE_BREW_TIME_KEY, null);
        return stored == null ? 400 : stored;
    }

    @Unique
    public void setRecipeBrewTime(int recipeBrewTime) {
        Preconditions.checkArgument(recipeBrewTime > 0, "recipeBrewTime must be positive");
        BrewingStandBlockEntity snapshot = this.paperarc$snapshot();
        if (snapshot == null) {
            return;
        }
        ApiState.put(snapshot, PAPERARC$RECIPE_BREW_TIME_KEY, recipeBrewTime);
    }

    @Unique
    private BrewingStandBlockEntity paperarc$snapshot() {
        // CraftBrewingStand 宿主是 CraftBlockEntityState 子类，@Shadow getSnapshot() 直访
        Object snapshot = this.getSnapshot();
        return snapshot instanceof BrewingStandBlockEntity bs ? bs : null;
    }
}
