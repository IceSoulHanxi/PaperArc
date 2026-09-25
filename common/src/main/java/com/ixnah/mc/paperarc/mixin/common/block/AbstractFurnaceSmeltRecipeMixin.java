package com.ixnah.mc.paperarc.mixin.common.block;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.izzel.arclight.common.bridge.core.world.item.crafting.RecipeHolderBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.inventory.CookingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * {@code FurnaceSmeltEvent#getRecipe()}（继承自 {@code BlockCookEvent}）的取值来源。
 *
 * <p>事件在 {@code burn} 里构造，配方是 {@code burn} 的形参 —— 但 <b>{@code burn} 的形态
 * 三端不一样</b>：Fabric 上它还是 vanilla 的 {@code private static}，Forge/NeoForge 的补丁
 * 把它改成了实例方法，`@WrapOperation` 的 handler 签名要多一个 receiver，一份源码满足不了两边
 * （实测：Forge 上 {@code InvalidInjectionException … expected
 * AbstractFurnaceBlockEntity at index 0}，启动即崩）。
 *
 * <p>改锚 {@code serverTick} 里那次 {@code quickCheck.getRecipeFor(...)} —— 纯 vanilla 代码、
 * 三端同形，与营火那条同款。它每 tick 都会返回配方（不只是烧成的那一 tick），
 * 所以在 {@code serverTick} 的两端清一次，别把值留给下一个方块实体。
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceSmeltRecipeMixin {

    @ModifyExpressionValue(method = "serverTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/RecipeManager$CachedCheck;getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeInput;Lnet/minecraft/server/level/ServerLevel;)Ljava/util/Optional;"))
    private static Optional<RecipeHolder<AbstractCookingRecipe>> paperarc$captureSmeltRecipe(
            Optional<RecipeHolder<AbstractCookingRecipe>> recipe) {
        PaperarcEventCauses.pushBlockCookRecipe(recipe
                .map(holder -> ((RecipeHolderBridge) (Object) holder).bridge$toBukkitRecipe())
                .filter(CookingRecipe.class::isInstance)
                .map(r -> (CookingRecipe<?>) r)
                .orElse(null));
        return recipe;
    }

    @Inject(method = "serverTick", at = @At("RETURN"))
    private static void paperarc$clearSmeltRecipe(ServerLevel level, BlockPos pos, BlockState state,
                                                  AbstractFurnaceBlockEntity blockEntity, CallbackInfo ci) {
        PaperarcEventCauses.popBlockCookRecipe();
    }
}
