package com.ixnah.mc.paperarc.mixin.common.block;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.bridge.core.item.crafting.IRecipeBridge;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.bukkit.inventory.CookingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * {@code FurnaceSmeltEvent#getRecipe()}（继承自 {@code BlockCookEvent}）的取值来源。
 *
 * <p>事件在 Arclight 覆写的 {@code burn} 里构造，但配方是 {@code burn} 的形参 ——
 * 与其注入 {@code burn} 本身（Arclight 把它覆写成了实例方法、与运行时的 static 不一致），
 * 不如在唯一调用方 {@code serverTick} 里 {@code @WrapOperation} 它的调用点，
 * 从实参直接拿到配方；调用点是 {@code invokestatic}，不受覆写体形态影响。
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceSmeltRecipeMixin {

    @WrapOperation(method = "serverTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;burn(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/crafting/Recipe;Lnet/minecraft/core/NonNullList;I)Z"))
    private static boolean paperarc$captureSmeltRecipe(AbstractFurnaceBlockEntity furnace,
                                                       RegistryAccess registryAccess, Recipe<?> recipe,
                                                       NonNullList<ItemStack> items, int maxStack,
                                                       Operation<Boolean> original) {
        EventCauseState.setBlockCookRecipe(paperarc$toCookingRecipe(recipe));
        try {
            return original.call(furnace, registryAccess, recipe, items, maxStack);
        } finally {
            EventCauseState.clearBlockCookRecipe();
        }
    }

    @Unique
    private static CookingRecipe<?> paperarc$toCookingRecipe(Recipe<?> recipe) {
        if (recipe == null) {
            return null;
        }
        org.bukkit.inventory.Recipe bukkit = ((IRecipeBridge) recipe).bridge$toBukkitRecipe();
        return bukkit instanceof CookingRecipe<?> cooking ? cooking : null;
    }
}
