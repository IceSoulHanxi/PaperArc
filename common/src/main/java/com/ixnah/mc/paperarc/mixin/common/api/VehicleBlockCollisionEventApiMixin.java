package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
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
 * <p>退回 {@code vehicle.getVelocity()} 不算实现：那时 {@code Entity#move} 已经把撞到的
 * 那个轴清零了。真正的来源是 {@code move} 的形参，由
 * {@code entity.EntityVehicleCollisionVelocityMixin} 在 HEAD 压 ThreadLocal。
 */
@Mixin(VehicleBlockCollisionEvent.class)
public abstract class VehicleBlockCollisionEventApiMixin {

    @Unique
    private Vector paperarc$velocity;

    @Inject(method = "<init>(Lorg/bukkit/entity/Vehicle;Lorg/bukkit/block/Block;)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$captureVelocity(Vehicle vehicle, Block block, CallbackInfo ci) {
        Vector velocity = PaperarcEventCauses.takeVehicleCollisionVelocity();
        this.paperarc$velocity = velocity != null ? velocity : vehicle.getVelocity();
    }

    @Unique
    public Vector getVelocity() {
        return this.paperarc$velocity;
    }
}
