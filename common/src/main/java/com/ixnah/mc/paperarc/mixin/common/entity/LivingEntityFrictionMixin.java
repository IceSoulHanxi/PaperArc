package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.LivingEntityFieldsBridge;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 让 {@code Frictional#setFrictionState} 在 NMS 侧真的生效（Paper 的 Friction-API）。
 *
 * <p>Paper 把 {@code shouldDiscardFriction()} 改写成
 * {@code !frictionState.toBooleanOrElse(!discardFriction)}：TriState 为 NOT_SET 时保持
 * vanilla 的 {@code discardFriction}，否则由 API 说了算（TRUE = 有摩擦 = 不丢弃）。
 * 这里用 {@code @ModifyReturnValue} 表达同一条式子，不覆写原方法。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityFrictionMixin {

    @ModifyReturnValue(method = "shouldDiscardFriction", at = @org.spongepowered.asm.mixin.injection.At("RETURN"))
    private boolean paperarc$applyFrictionState(boolean original) {
        return !((LivingEntityFieldsBridge) this).paper$getFrictionState().toBooleanOrElse(!original);
    }
}
