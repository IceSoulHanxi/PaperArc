package com.ixnah.mc.paperarc.mixin.common.block;

import com.ixnah.mc.paperarc.bridge.BeaconBlockEntityBridge;
import com.ixnah.mc.paperarc.bridge.BeaconEffectRangeState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 让 {@code Beacon#setEffectRange} 真的生效并落盘（gaps.md E7）。
 *
 * <p>vanilla 的 {@code applyEffects} 是 {@code private static}，形参里没有方块实体，
 * 半径写死 {@code levels * 10 + 10}；Paper 给它加了一个形参，我们改不了签名。
 * 做法：唯一调用方 {@code tick} 的 HEAD 把这台信标的自定义半径压 ThreadLocal，
 * {@code applyEffects} 里用 {@code @ModifyVariable} 把算出来的半径换掉，
 * {@code tick} 的 RETURN 清掉。没设自定义半径（-1）时不压值，行为与 vanilla 逐字一致。
 *
 * <p>落盘用 Paper 的键 {@code Paper.Range}，只在设过自定义半径时才写。
 */
@Mixin(BeaconBlockEntity.class)
public abstract class BeaconEffectRangeMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private static void paperarc$pushEffectRange(Level level, BlockPos pos, BlockState state,
                                                 BeaconBlockEntity beacon, CallbackInfo ci) {
        double range = ((BeaconBlockEntityBridge) beacon).paper$getEffectRange();
        if (range >= 0) {
            BeaconEffectRangeState.push(range);
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private static void paperarc$popEffectRange(Level level, BlockPos pos, BlockState state,
                                                BeaconBlockEntity beacon, CallbackInfo ci) {
        BeaconEffectRangeState.pop();
    }

    /**
     * {@code applyEffects} 里算出的作用半径（{@code levels * 10 + 10}）。
     * {@code @ModifyVariable} 按类型 + ordinal 取：这个方法里只有一个 double 局部，
     * 不存在 1.21.1 之外的歧义（`javap -c -l` 核对）。
     */
    @ModifyVariable(method = "applyEffects", at = @At(value = "STORE"), ordinal = 0)
    private static double paperarc$applyCustomRange(double vanillaRange) {
        Double custom = BeaconEffectRangeState.peek();
        return custom == null ? vanillaRange : custom;
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"))
    private void paperarc$saveEffectRange(CompoundTag nbt, HolderLookup.Provider registries, CallbackInfo ci) {
        double range = ((BeaconBlockEntityBridge) this).paper$getEffectRange();
        if (range >= 0) {
            nbt.putDouble("Paper.Range", range);
        }
    }

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    private void paperarc$loadEffectRange(CompoundTag nbt, HolderLookup.Provider registries, CallbackInfo ci) {
        if (nbt.contains("Paper.Range", Tag.TAG_DOUBLE)) {
            ((BeaconBlockEntityBridge) this).paper$setEffectRange(nbt.getDouble("Paper.Range"));
        }
    }
}
