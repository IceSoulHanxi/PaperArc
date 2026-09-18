package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.EntityBridge;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * paper 的 Friction-API 在生物侧的消费方：
 * {@code shouldDiscardFriction()} 由 {@code frictionState} 覆写
 * （{@code TRUE} = 保留摩擦力 → 不丢弃；{@code FALSE} = 丢弃；{@code NOT_SET} = 原样）。
 *
 * <p>没有这一半的话 {@code setFrictionState} 就是存了个没人读的值。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityFrictionMixin {

    @ModifyReturnValue(method = "shouldDiscardFriction", at = @At("RETURN"))
    private boolean paperarc$applyFrictionState(boolean discard) {
        // Paper: return !this.frictionState.toBooleanOrElse(!this.discardFriction);
        return !((EntityBridge) this).paper$frictionState().toBooleanOrElse(!discard);
    }
}
