package com.ixnah.mc.paperarc.mixin.common.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent;
import net.minecraft.world.entity.Entity;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Add-entity-knockback-events.patch: the ravager's melee knockback
 * ({@code Ravager#strongKnockback(Entity)}) pushes its target with the ravager as source.
 */
@Mixin(net.minecraft.world.entity.monster.Ravager.class)
public abstract class RavagerMixin {

    @WrapOperation(
            method = "strongKnockback(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;push(DDD)V"
            )
    )
    private void paperarc$pushedByAttack(Entity instance, double x, double y, double z,
                                         Operation<Void> original, Entity target) {
        EntityPushedByEntityAttackEvent event = new EntityPushedByEntityAttackEvent(
                PaperArcBridge.bukkitEntity(instance),
                                PaperArcBridge.bukkitEntity((Entity) (Object) this),
                new Vector(x, y, z));
        if (!event.callEvent()) {
            return;
        }
        Vector knockback = event.getAcceleration();
        original.call(instance, knockback.getX(), knockback.getY(), knockback.getZ());
    }
}
