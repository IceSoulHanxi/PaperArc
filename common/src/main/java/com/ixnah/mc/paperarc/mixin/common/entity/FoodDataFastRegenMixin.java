package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * {@code EntityRegainHealthEvent#isFastRegen()} 的取值来源。
 *
 * <p>{@code FoodData#tick} 里有两处 {@code Player#heal(float)}：饱和度那支
 * （快速回血，paper 传 {@code isFastRegen = true}）与 80 tick 一次的慢回血。
 * **不按 ordinal 区分**（docs/mixin-conventions.md 的 `@Local`/ordinal 教训）：
 * 快速那支前面紧挨着 {@code Math.min(saturationLevel, 6.0F)}、后面紧挨着
 * {@code addExhaustion}，用这两个结构性标记做 slice（`javap -c` 核对 1.21.1 的
 * {@code tick(Player)}：offset 152 Math.min → 163 heal → 169 addExhaustion）。
 */
@Mixin(FoodData.class)
public abstract class FoodDataFastRegenMixin {

    @WrapOperation(method = "tick",
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Ljava/lang/Math;min(FF)F", remap = false),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;addExhaustion(F)V")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;heal(F)V"))
    private void paperarc$markFastRegen(Player player, float amount, Operation<Void> original) {
        PaperarcEventCauses.pushFastRegen(true);
        try {
            original.call(player, amount);
        } finally {
            PaperarcEventCauses.popFastRegen();
        }
    }
}
