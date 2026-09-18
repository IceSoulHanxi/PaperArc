package com.ixnah.mc.paperarc.mixin.mojmap.block;

import com.ixnah.mc.paperarc.bridge.BeaconBlockEntityBridge;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * 注入 Paper 的 {@code BeaconBlockEntity.effectRange}（{@code Beacon#getEffectRange} 一族，A5-3）。
 * {@code -1} 表示"按信标层数算"，与 Paper 的哨兵值一致。
 */
@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityFieldsMixin implements BeaconBlockEntityBridge {

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
}
