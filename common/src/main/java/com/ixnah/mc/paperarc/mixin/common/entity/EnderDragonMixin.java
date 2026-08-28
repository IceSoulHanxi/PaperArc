package com.ixnah.mc.paperarc.mixin.common.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent;
import net.minecraft.world.entity.Entity;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * Port of Add-entity-knockback-events.patch: the dragon's body knockback
 * ({@code EnderDragon#knockBack(List)}) pushes entities with the dragon as
 * source.
 *
 * <p>1.20.1 signature is the single-argument {@code knockBack(List<Entity>)}
 * (the {@code (ServerLevel, List)} two-arg form only exists from 1.21 on).
 */
@Mixin(net.minecraft.world.entity.boss.enderdragon.EnderDragon.class)
public abstract class EnderDragonMixin {

    @WrapOperation(
            method = "knockBack(Ljava/util/List;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;push(DDD)V"
            )
    )
    private void paperarc$pushedByAttack(Entity instance, double x, double y, double z,
                                         Operation<Void> original, List<Entity> entities) {
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