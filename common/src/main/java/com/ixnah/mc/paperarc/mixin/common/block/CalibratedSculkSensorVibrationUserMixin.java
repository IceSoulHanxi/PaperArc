package com.ixnah.mc.paperarc.mixin.common.block;

import com.ixnah.mc.paperarc.bridge.SculkSensorRangeBridge;
import net.minecraft.world.level.block.entity.CalibratedSculkSensorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 校准型潜声传感器自己覆盖了 {@code getListenerRadius()}（返回 16），
 * 所以父类那一发 {@code @Inject} 照不到它，这里再来一发。
 *
 * <p>状态字段由父类 mixin 合并进
 * {@code SculkSensorBlockEntity$VibrationUser}，本类继承之后走桥接口读取
 * （不 {@code @Shadow} 父类成员 —— 见 docs/mixin-conventions.md）。</p>
 */
@Mixin(CalibratedSculkSensorBlockEntity.VibrationUser.class)
public abstract class CalibratedSculkSensorVibrationUserMixin {

    @Inject(method = "getListenerRadius", at = @At("HEAD"), cancellable = true)
    private void paperarc$overrideListenerRadius(CallbackInfoReturnable<Integer> cir) {
        Integer override = ((SculkSensorRangeBridge) this).paperarc$getRangeOverride();
        if (override != null) {
            cir.setReturnValue(override);
        }
    }
}
