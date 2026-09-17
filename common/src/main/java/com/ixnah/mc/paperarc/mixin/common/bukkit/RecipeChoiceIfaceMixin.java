package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.inventory.RecipeChoice;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.material.MaterialData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-3：{@link org.bukkit.inventory.RecipeChoice} 的 paper default 方法体（照抄 paper-api）。
 */
@Mixin(targets = "org.bukkit.inventory.RecipeChoice", remap = false)
public interface RecipeChoiceIfaceMixin {

    @Unique
    public default RecipeChoice validate(boolean allowEmptyRecipes) {
        RecipeChoice self = (RecipeChoice) this;
        return self;
    }
}
