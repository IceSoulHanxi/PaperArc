package com.ixnah.mc.paperarc.mixin.common.block;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.izzel.arclight.common.bridge.core.item.crafting.IRecipeBridge;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import org.bukkit.inventory.CookingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

/**
 * {@code BlockCookEvent#getRecipe()} 在营火这一路的取值来源。
 *
 * <p>Arclight 把 {@code cookTick} 整个 {@code @Overwrite} 掉，事件在覆写体里构造，
 * 但覆写体里那次 {@code quickCheck.getRecipeFor(...)} 原样保留（`javap` 核对覆写产物），
 * 把它的返回值截下来压进 {@link EventCauseState} 即可，不改变返回值本身。
 */
@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityCookRecipeMixin {

    @ModifyExpressionValue(method = "cookTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/RecipeManager$CachedCheck;getRecipeFor(Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;"))
    private static Optional<CampfireCookingRecipe> paperarc$captureCookRecipe(Optional<CampfireCookingRecipe> recipe) {
        EventCauseState.setBlockCookRecipe(recipe
                .map(r -> ((IRecipeBridge) r).bridge$toBukkitRecipe())
                .filter(CookingRecipe.class::isInstance)
                .map(r -> (CookingRecipe<?>) r)
                .orElse(null));
        return recipe;
    }
}
