package com.ixnah.mc.paperarc.mixin.common.api;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.google.common.base.Preconditions;

import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBrewingStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's Add-recipeBrewTime.patch additions on {@link CraftBrewingStand}:
 * {@code BrewingStand#getRecipeBrewTime()} / {@code setRecipeBrewTime(int)}.
 *
 * <p>Paper's implementation reads/writes the public int field
 * {@code recipeBrewTime} (default 400) which it adds to
 * {@link BrewingStandBlockEntity} — that field only exists in the patched
 * runtime jar, not in the vanilla mojmap compile jar, so it is accessed via
 * reflection. {@code CraftBlockEntityState#getSnapshot()} is protected and
 * cannot be shadowed from a subclass-target mixin either, hence reflection too.
 */
@Mixin(CraftBrewingStand.class)
public abstract class CraftBrewingStandApiMixin {

    @Unique
    private static final String PAPERARC$SNAPSHOT_OWNER = "org.bukkit.craftbukkit.v1_20_R1.block.CraftBlockEntityState";

    @Unique
    private static Method paperarc$snapshotMethod;

    @Unique
    private static Field paperarc$recipeBrewTimeField;

    @Unique
    public int getRecipeBrewTime() {
        BrewingStandBlockEntity snapshot = this.paperarc$snapshot();
        if (snapshot == null) {
            return 400; // Paper/vanilla default recipe brew time
        }
        try {
            return paperarc$recipeBrewTimeField().getInt(snapshot);
        } catch (ReflectiveOperationException e) {
            return 400; // unpatched runtime: vanilla hardcodes 400 ticks
        }
    }

    @Unique
    public void setRecipeBrewTime(int recipeBrewTime) {
        Preconditions.checkArgument(recipeBrewTime > 0, "recipeBrewTime must be positive");
        BrewingStandBlockEntity snapshot = this.paperarc$snapshot();
        if (snapshot == null) {
            return;
        }
        try {
            paperarc$recipeBrewTimeField().setInt(snapshot, recipeBrewTime);
        } catch (ReflectiveOperationException e) {
            // unpatched runtime: nothing to persist, vanilla keeps 400 ticks
        }
    }

    @Unique
    private BrewingStandBlockEntity paperarc$snapshot() {
        try {
            if (paperarc$snapshotMethod == null) {
                Method method = Class.forName(PAPERARC$SNAPSHOT_OWNER).getDeclaredMethod("getSnapshot");
                method.setAccessible(true);
                paperarc$snapshotMethod = method;
            }
            return (BrewingStandBlockEntity) paperarc$snapshotMethod.invoke(this);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("PaperArc: cannot access CraftBlockEntityState#getSnapshot()", e);
        }
    }

    @Unique
    private static Field paperarc$recipeBrewTimeField() throws NoSuchFieldException {
        if (paperarc$recipeBrewTimeField == null) {
            Field field = BrewingStandBlockEntity.class.getField("recipeBrewTime"); // public in Paper's runtime patch
            field.setAccessible(true);
            paperarc$recipeBrewTimeField = field;
        }
        return paperarc$recipeBrewTimeField;
    }
}
