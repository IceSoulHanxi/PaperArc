package com.ixnah.mc.paperarc.mixin.common.block;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.izzel.arclight.common.bridge.core.world.item.crafting.RecipeHolderBridge;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import org.bukkit.inventory.CookingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

/**
 * {@code BlockCookEvent#getRecipe()} 在营火这一路的取值来源。
 *
 * <p>Arclight 在 {@code cookTick} 里构造事件，但那次
 * {@code quickCheck.getRecipeFor(...)} 原样保留，把它的返回值截下来压 ThreadLocal 即可，
 * 不改变返回值本身。1.21.1 的返回类型是 {@code Optional<RecipeHolder<CampfireCookingRecipe>>}
 * （1.20.1 是裸 {@code Optional<CampfireCookingRecipe>}），
 * 转 Bukkit 走 Arclight 的 {@code RecipeHolderBridge}。
 */
@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityCookRecipeMixin {

    @ModifyExpressionValue(method = "cookTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/RecipeManager$CachedCheck;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/server/level/ServerLevel;)Ljava/util/Optional;"))
    private static Optional<RecipeHolder<CampfireCookingRecipe>> paperarc$captureCookRecipe(
            Optional<RecipeHolder<CampfireCookingRecipe>> recipe) {
        PaperarcEventCauses.pushBlockCookRecipe(recipe
                .map(holder -> ((RecipeHolderBridge) (Object) holder).bridge$toBukkitRecipe())
                .filter(CookingRecipe.class::isInstance)
                .map(r -> (CookingRecipe<?>) r)
                .orElse(null));
        return recipe;
    }
}
