package com.ixnah.mc.paperarc.mixin.common.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Add-entity-knockback-events.patch: arrow knockback pushes its
 * target with the arrow as source.
 *
 * <p>1.20.1 has no {@code doKnockback} method (added in 1.21); the push call
 * lives inline inside {@code AbstractArrow#onHitEntity(EntityHitResult)}.
 */
@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {

    @WrapOperation(
            method = "onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;push(DDD)V"
            )
    )
    private void paperarc$pushedByAttack(LivingEntity instance, double x, double y, double z,
                                         Operation<Void> original, EntityHitResult result) {
        EntityPushedByEntityAttackEvent event = new EntityPushedByEntityAttackEvent(
                PaperArcBridge.bukkitEntity(instance),
                PaperArcBridge.bukkitEntity((AbstractArrow) (Object) this),
                new Vector(x, y, z));
        if (!event.callEvent()) {
            return;
        }
        Vector knockback = event.getAcceleration();
        original.call(instance, knockback.getX(), knockback.getY(), knockback.getZ());
    }
}