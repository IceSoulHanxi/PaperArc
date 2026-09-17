package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.ItemEntityBridge;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.kyori.adventure.util.TriState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 让 {@code Item#setFrictionState} 在 NMS 侧真的生效（Paper 的 Friction-API）。
 *
 * <p>Paper 在 {@code ItemEntity.tick} 里把摩擦系数 {@code f1} 在 FRICTION=FALSE 时改成
 * {@code 1F}；{@code f1} 只被用在紧接着的
 * {@code getDeltaMovement().multiply(f1, 0.98, f1)} 上（tick 里的第一个 multiply，
 * 第二个是落地反弹那段），所以直接改这次调用的 x/z 实参，等价且不碰局部变量表。
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityFrictionMixin {

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/world/phys/Vec3;multiply(DDD)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 paperarc$applyFrictionState(Vec3 delta, double x, double y, double z, Operation<Vec3> original) {
        if (((ItemEntityBridge) this).paper$getFrictionState() == TriState.FALSE) {
            return original.call(delta, 1.0D, y, 1.0D);
        }
        return original.call(delta, x, y, z);
    }
}
