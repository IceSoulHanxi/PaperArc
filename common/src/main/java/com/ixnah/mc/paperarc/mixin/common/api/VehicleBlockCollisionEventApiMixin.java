package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import org.bukkit.block.Block;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code VehicleBlockCollisionEvent#getVelocity()}（撞上之前的移动向量）。
 *
 * <p>取不到"撞前"的值就只能返回 {@code vehicle.getVelocity()}，而那时
 * {@code Entity#move} 已经把撞到的那个轴清零了 —— 恒返回被削过的向量属于
 * "返回默认值"。真正的来源是 {@code Entity#move} 的形参，由
 * {@code EntityVehicleCollisionVelocityMixin} 在 HEAD 压进 {@link EventCauseState}。
 */
@Mixin(VehicleBlockCollisionEvent.class)
public abstract class VehicleBlockCollisionEventApiMixin {

    @Unique
    private Vector paperarc$velocity;

    @Inject(method = "<init>(Lorg/bukkit/entity/Vehicle;Lorg/bukkit/block/Block;)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$captureVelocity(Vehicle vehicle, Block block, CallbackInfo ci) {
        Vector velocity = EventCauseState.takeVehicleCollisionVelocity();
        this.paperarc$velocity = velocity != null ? velocity : vehicle.getVelocity();
    }

    @Unique
    public Vector getVelocity() {
        return this.paperarc$velocity;
    }
}
