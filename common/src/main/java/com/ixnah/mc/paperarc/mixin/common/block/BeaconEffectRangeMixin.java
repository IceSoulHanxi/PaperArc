package com.ixnah.mc.paperarc.mixin.common.block;

import com.ixnah.mc.paperarc.bridge.BeaconBlockEntityBridge;
import com.ixnah.mc.paperarc.bridge.BeaconEffectRangeState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * gaps.md E7：让 {@code Beacon#setEffectRange} 真的改变作用半径，而不只是存个值。
 *
 * <p>vanilla 的 {@code applyEffects(Level, BlockPos, int, MobEffect, MobEffect)} 里
 * {@code double d0 = levels * 10 + 10;}（srg jar javap：唯一一处 {@code dstore 5}），
 * Paper 的做法是给方法加一个 {@code BeaconBlockEntity} 形参再读它的自定义半径。
 * 我们改不了签名，于是在唯一调用方 {@code tick} 里把半径压进
 * {@link BeaconEffectRangeState}，这里 {@code @ModifyVariable} 读回。
 *
 * <p>{@code index = 5} 的槽号来自官方名映射后的 forge jar 的 LocalVariableTable，
 * 1.20.1 只有 Forge 一个加载器（checklist bj）。
 */
@Mixin(BeaconBlockEntity.class)
public abstract class BeaconEffectRangeMixin {

    @WrapOperation(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/BeaconBlockEntity;applyEffects(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ILnet/minecraft/world/effect/MobEffect;Lnet/minecraft/world/effect/MobEffect;)V"))
    private static void paperarc$pushEffectRange(Level level, BlockPos pos, int levels, MobEffect primary,
                                                 MobEffect secondary, Operation<Void> original,
                                                 Level tickLevel, BlockPos tickPos,
                                                 net.minecraft.world.level.block.state.BlockState tickState,
                                                 BeaconBlockEntity blockEntity) {
        double range = ((BeaconBlockEntityBridge) blockEntity).paper$getEffectRange();
        if (range < 0.0D) {
            original.call(level, pos, levels, primary, secondary);
            return;
        }
        BeaconEffectRangeState.set(range);
        try {
            original.call(level, pos, levels, primary, secondary);
        } finally {
            BeaconEffectRangeState.clear();
        }
    }

    @ModifyVariable(method = "applyEffects", at = @At("STORE"), index = 5)
    private static double paperarc$applyEffectRange(double vanillaRange) {
        Double custom = BeaconEffectRangeState.get();
        return custom == null ? vanillaRange : custom;
    }
}
