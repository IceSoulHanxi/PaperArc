package com.ixnah.mc.paperarc.mixin.common.block;

import com.ixnah.mc.paperarc.bridge.SculkSensorRangeBridge;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让 Paper 的 listener-range 覆盖值真的生效：
 * {@code SculkSensorBlockEntity$VibrationUser#getListenerRadius()}。
 *
 * <p>不 {@code @Shadow} 内部类的外层引用（它在 mojmap 里仍是
 * {@code field_44618} 这样的中间名，三个 loader 的重映射结果不一致），
 * 改由 {@code SculkSensorBlockEntityFieldsMixin} 在写入时把值推进来。</p>
 */
@Mixin(SculkSensorBlockEntity.VibrationUser.class)
public abstract class SculkSensorVibrationUserMixin implements SculkSensorRangeBridge {

    @Unique
    private Integer paperarc$rangeOverride;

    @Override
    public Integer paperarc$getRangeOverride() {
        return this.paperarc$rangeOverride;
    }

    @Override
    public void paperarc$setRangeOverride(Integer rangeOverride) {
        this.paperarc$rangeOverride = rangeOverride;
    }

    @Inject(method = "getListenerRadius", at = @At("HEAD"), cancellable = true)
    private void paperarc$overrideListenerRadius(CallbackInfoReturnable<Integer> cir) {
        Integer override = this.paperarc$rangeOverride;
        if (override != null) {
            cir.setReturnValue(override);
        }
    }
}
