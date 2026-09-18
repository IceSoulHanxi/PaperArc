package com.ixnah.mc.paperarc.mixin.mojmap.block;

import com.ixnah.mc.paperarc.bridge.SculkSensorRangeBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 监听范围覆盖值的 NBT 持久化，键名与 Paper 一致（{@code Paper.ListenerRange}），
 * 这样和 Paper 存的世界互通。
 *
 * <p>同时这条 NBT 往返就是 Craft 快照 → 真实方块实体的回写通道：
 * {@code CraftBlockEntityState#update} 走 {@code load(snapshot.saveWithoutMetadata())}。
 *
 * <p>与 Paper 的偏差：Paper 只在覆盖值**不等于 vanilla 默认**时写 NBT，这里非 null 就写
 * （普通 8 / 校准 16 两个默认值分散在两个内部类里，判默认要多一次跨类查询，
 * 而多写一个 int 的效果完全一样）。
 */
@Mixin(SculkSensorBlockEntity.class)
public abstract class SculkSensorBlockEntityNbtMixin {

    @Unique
    private static final String PAPERARC$LISTENER_RANGE_KEY = "Paper.ListenerRange";

    @Shadow
    public abstract net.minecraft.world.level.gameevent.vibrations.VibrationSystem.User getVibrationUser();

    @Unique
    private SculkSensorRangeBridge paperarc$range() {
        return (SculkSensorRangeBridge) this.getVibrationUser();
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void paperarc$loadListenerRange(CompoundTag nbt, CallbackInfo ci) {
        this.paperarc$range().paperarc$setRangeOverride(
                nbt.contains(PAPERARC$LISTENER_RANGE_KEY)
                        ? nbt.getInt(PAPERARC$LISTENER_RANGE_KEY) : null);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void paperarc$saveListenerRange(CompoundTag nbt, CallbackInfo ci) {
        Integer override = this.paperarc$range().paperarc$getRangeOverride();
        if (override != null) {
            nbt.putInt(PAPERARC$LISTENER_RANGE_KEY, override);
        }
    }
}
