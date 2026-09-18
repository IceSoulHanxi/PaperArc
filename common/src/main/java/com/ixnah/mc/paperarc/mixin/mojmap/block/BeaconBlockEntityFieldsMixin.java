package com.ixnah.mc.paperarc.mixin.mojmap.block;

import com.ixnah.mc.paperarc.bridge.BeaconBlockEntityBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 注入 Paper 的 {@code BeaconBlockEntity.effectRange}（{@code Beacon#getEffectRange} 一族，A5-3）。
 * {@code -1} 表示"按信标层数算"，与 Paper 的哨兵值一致。
 *
 * <p>A8/Y-3 补落盘：键名 {@code Paper.Range}，与 Paper 一致（存档互通）。
 * 让半径真正生效的那一半见 {@code BeaconEffectRangeMixin}。
 */
@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityFieldsMixin implements BeaconBlockEntityBridge {

    @Unique
    private static final String PAPERARC$RANGE_TAG = "Paper.Range";

    @Unique
    public double effectRange = -1.0D; // Paper

    @Override
    public double paper$getEffectRange() {
        return this.effectRange;
    }

    @Override
    public void paper$setEffectRange(double range) {
        this.effectRange = range;
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"))
    private void paperarc$saveEffectRange(CompoundTag nbt, CallbackInfo ci) {
        nbt.putDouble(PAPERARC$RANGE_TAG, this.effectRange);
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void paperarc$loadEffectRange(CompoundTag nbt, CallbackInfo ci) {
        this.effectRange = nbt.contains(PAPERARC$RANGE_TAG, Tag.TAG_DOUBLE)
                ? nbt.getDouble(PAPERARC$RANGE_TAG)
                : -1.0D;
    }
}
