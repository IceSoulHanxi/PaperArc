package com.ixnah.mc.paperarc.mixin.common.block;

import com.ixnah.mc.paperarc.bridge.BeaconBlockEntityBridge;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code BeaconBlockEntity.effectRange} supplementary field.
 * Field name matches Paper exactly (no {@code paperarc$} prefix) for reflection
 * ABI compatibility; {@code -1} = 未设置，按信标等级算默认范围。
 */
@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityFieldsMixin implements BeaconBlockEntityBridge {

    @Unique
    public double effectRange = -1; // Paper

    @Override
    public double paper$getEffectRange() {
        return this.effectRange;
    }

    @Override
    public void paper$setEffectRange(double effectRange) {
        this.effectRange = effectRange;
    }
}
