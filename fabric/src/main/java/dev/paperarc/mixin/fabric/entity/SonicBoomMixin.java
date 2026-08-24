package dev.paperarc.mixin.fabric.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Add-entity-knockback-events.patch: the warden's sonic boom pushes its target with the
 * warden as the pushing entity, so fire {@link EntityPushedByEntityAttackEvent}
 * ({@code Cause.PUSH}) first; when cancelled the vanilla push is skipped entirely.
 */
@Mixin(net.minecraft.world.entity.ai.behavior.warden.SonicBoom.class)
public abstract class SonicBoomMixin {

    @WrapOperation(
            // The push happens inside a lambda captured from tick(Warden,...); warden is its first param.
            method = "method_43265(Lnet/minecraft/class_7260;Lnet/minecraft/class_3218;Lnet/minecraft/class_1309;)V",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;push(DDD)V"
            )
    )
    private static void paperarc$pushedByAttack(Entity instance, double x, double y, double z,
                                         Operation<Void> original, Warden warden, ServerLevel level, LivingEntity target) {
        Vector delta = new Vector(x, y, z);
        EntityPushedByEntityAttackEvent event = new EntityPushedByEntityAttackEvent(
                PaperArcBridge.bukkitEntity(instance),
                EntityKnockbackEvent.Cause.PUSH,
                PaperArcBridge.bukkitEntity(warden),
                delta);
        if (!event.callEvent()) {
            return;
        }
        Vector knockback = event.getKnockback();
        original.call(instance, knockback.getX(), knockback.getY(), knockback.getZ());
    }
}
