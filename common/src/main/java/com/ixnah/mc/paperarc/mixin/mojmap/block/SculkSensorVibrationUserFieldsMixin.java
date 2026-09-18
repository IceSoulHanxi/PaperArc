package com.ixnah.mc.paperarc.mixin.mojmap.block;

import com.ixnah.mc.paperarc.bridge.SculkSensorRangeBridge;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper 的 Configurable-sculk-sensor-listener-range：注入范围覆盖字段并让
 * {@code getListenerRadius()} 优先返回它（vanilla 普通感测器恒 8）。
 * 存放位置的取舍见 {@link SculkSensorRangeBridge}。
 */
@Mixin(targets = "net.minecraft.world.level.block.entity.SculkSensorBlockEntity$VibrationUser")
public abstract class SculkSensorVibrationUserFieldsMixin implements SculkSensorRangeBridge {

    @Unique
    public Integer paperarc$rangeOverride; // Paper: rangeOverride

    @Override
    public Integer paperarc$getRangeOverride() {
        return this.paperarc$rangeOverride;
    }

    @Override
    public void paperarc$setRangeOverride(Integer range) {
        this.paperarc$rangeOverride = range;
    }

    @ModifyReturnValue(method = "getListenerRadius", at = @At("RETURN"))
    private int paperarc$listenerRangeOverride(int original) {
        return this.paperarc$rangeOverride != null ? this.paperarc$rangeOverride : original;
    }
}
