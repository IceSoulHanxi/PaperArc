package com.ixnah.mc.paperarc.mixin.mojmap.block;

import com.ixnah.mc.paperarc.bridge.SculkSensorRangeBridge;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 校准 sculk 感测器**重写**了 {@code getListenerRadius()}（vanilla 恒 16），
 * 所以父类那份 {@code @ModifyReturnValue} 照不到它，必须单独再挂一个。
 * 覆盖值字段本身继承自 {@link SculkSensorVibrationUserFieldsMixin}，经
 * {@link SculkSensorRangeBridge} 读取（不 @Shadow 父类成员，见状态文档第五章）。
 */
@Mixin(targets = "net.minecraft.world.level.block.entity.CalibratedSculkSensorBlockEntity$VibrationUser")
public abstract class CalibratedSculkSensorVibrationUserMixin {

    @ModifyReturnValue(method = "getListenerRadius", at = @At("RETURN"))
    private int paperarc$listenerRangeOverride(int original) {
        Integer override = ((SculkSensorRangeBridge) this).paperarc$getRangeOverride();
        return override != null ? override : original;
    }
}
