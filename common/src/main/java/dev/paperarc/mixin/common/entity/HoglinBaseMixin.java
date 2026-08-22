package dev.paperarc.mixin.common.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Add-entity-knockback-events.patch: the hoglin's throw
 * ({@code HoglinBase#throwTarget(LivingEntity attacker, LivingEntity target)}) pushes the target
 * with the hoglin as source.
 */
@Mixin(net.minecraft.world.entity.monster.hoglin.HoglinBase.class)
public abstract class HoglinBaseMixin {

    @WrapOperation(
            method = "throwTarget(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;push(DDD)V"
            )
    )
    private void paperarc$pushedByAttack(LivingEntity instance, double x, double y, double z,
                                         Operation<Void> original, LivingEntity attacker, LivingEntity target) {
        EntityPushedByEntityAttackEvent event = new EntityPushedByEntityAttackEvent(
                PaperArcBridge.bukkitEntity(instance),
                EntityKnockbackEvent.Cause.PUSH,
                PaperArcBridge.bukkitEntity(attacker),
                new Vector(x, y, z));
        if (!event.callEvent()) {
            return;
        }
        Vector knockback = event.getKnockback();
        original.call(instance, knockback.getX(), knockback.getY(), knockback.getZ());
    }
}
