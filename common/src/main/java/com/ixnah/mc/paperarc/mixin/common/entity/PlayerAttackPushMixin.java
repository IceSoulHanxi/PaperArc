package com.ixnah.mc.paperarc.mixin.common.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Add-entity-knockback-events.patch: the non-living branch of {@code Player#attack}
 * pushes the target with the attacker as source. (The living-target knockback path is covered by
 * LivingEntityKnockbackMixin via the runtime 5-arg knockback overload.)
 */
@Mixin(Player.class)
public abstract class PlayerAttackPushMixin {

    @WrapOperation(
            method = "attack(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;push(DDD)V"
            )
    )
    private void paperarc$pushedByAttack(Entity instance, double x, double y, double z,
                                         Operation<Void> original, Entity target) {
        EntityPushedByEntityAttackEvent event = new EntityPushedByEntityAttackEvent(
                PaperArcBridge.bukkitEntity(instance),
                EntityKnockbackEvent.Cause.PUSH,
                PaperArcBridge.bukkitPlayer((Player) (Object) this),
                new Vector(x, y, z));
        if (!event.callEvent()) {
            return;
        }
        Vector knockback = event.getKnockback();
        original.call(instance, knockback.getX(), knockback.getY(), knockback.getZ());
    }
}
