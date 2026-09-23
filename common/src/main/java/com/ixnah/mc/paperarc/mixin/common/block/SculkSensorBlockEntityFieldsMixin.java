package com.ixnah.mc.paperarc.mixin.common.block;

import com.ixnah.mc.paperarc.bridge.SculkSensorRangeBridge;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paper 的 {@code SculkSensorBlockEntity.rangeOverride} + NBT 键
 * {@code Paper.ListenerRange}（Configurable-sculk-sensor-listener-range.patch）。
 * 字段名与 NBT 键都对齐 Paper。
 *
 * <p>写入时同步推给 {@code VibrationUser}，由
 * {@code SculkSensorVibrationUserMixin} 让 {@code getListenerRadius()} 生效。</p>
 */
@Mixin(SculkSensorBlockEntity.class)
public abstract class SculkSensorBlockEntityFieldsMixin implements SculkSensorRangeBridge {

    @Unique
    private static final String PAPERARC$LISTENER_RANGE_NBT_KEY = "Paper.ListenerRange";

    @Unique
    public Integer rangeOverride = null; // Paper

    @Shadow
    public abstract VibrationSystem.User getVibrationUser();

    @Override
    public Integer paperarc$getRangeOverride() {
        return this.rangeOverride;
    }

    @Override
    public void paperarc$setRangeOverride(Integer rangeOverride) {
        this.rangeOverride = rangeOverride;
        VibrationSystem.User user = this.getVibrationUser();
        if (user instanceof SculkSensorRangeBridge bridge) {
            bridge.paperarc$setRangeOverride(rangeOverride);
        }
    }

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    private void paperarc$loadRangeOverride(ValueInput in, CallbackInfo ci) {
        this.paperarc$setRangeOverride(in.getInt(PAPERARC$LISTENER_RANGE_NBT_KEY).orElse(null));
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"))
    private void paperarc$saveRangeOverride(ValueOutput out, CallbackInfo ci) {
        // Paper 只在"与该类型传感器的默认半径不同"时写盘；这里的 getListenerRadius()
        // 已经被覆盖值改写，比不出默认值，所以只要设过就写（多写一个 int，语义等价）。
        Integer override = this.rangeOverride;
        if (override != null) {
            out.putInt(PAPERARC$LISTENER_RANGE_NBT_KEY, override);
        }
    }
}
