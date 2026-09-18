package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.v.util.CraftVector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code VehicleBlockCollisionEvent#getVelocity()} 的取值来源。
 *
 * <p>Arclight 在 {@code Entity#move} 里撞墙那一支构造事件，构造点在它自己的注入器
 * handler 里（选不了，见 docs/mixin-conventions.md），所以在 {@code move} 的 HEAD 把形参
 * （= 这次移动的原始向量，paper 的 {@code moveVector}）压进 ThreadLocal。
 *
 * <p>{@code move} 是每实体每 tick 的热路径，先用两个 {@code instanceof} 把非载具挡掉
 * （实现 bukkit {@code Vehicle} 的只有船和矿车两族），不去碰 {@code getBukkitEntity()}
 * （那会给每个实体现造一个 Craft 包装）。
 */
@Mixin(Entity.class)
public abstract class EntityVehicleCollisionVelocityMixin {

    @Inject(method = "move", at = @At("HEAD"))
    private void paperarc$captureMoveVector(MoverType type, Vec3 movement, CallbackInfo ci) {
        if ((Object) this instanceof Boat || (Object) this instanceof AbstractMinecart) {
            PaperarcEventCauses.pushVehicleCollisionVelocity(CraftVector.toBukkit(movement));
        }
    }

    @Inject(method = "move", at = @At("RETURN"))
    private void paperarc$clearMoveVector(MoverType type, Vec3 movement, CallbackInfo ci) {
        if ((Object) this instanceof Boat || (Object) this instanceof AbstractMinecart) {
            PaperarcEventCauses.popVehicleCollisionVelocity();
        }
    }
}
