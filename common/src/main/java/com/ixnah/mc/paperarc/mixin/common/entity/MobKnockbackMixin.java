package com.ixnah.mc.paperarc.mixin.common.entity;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper 1.20.1's Implement-EntityKnockbackByEntityEvent-and-EntityPus.patch
 * (knockback half, Mob side). Fires {@link EntityKnockbackByEntityEvent} at the
 * {@code LivingEntity#knockback(DDD)} call site inside {@code Mob.doHurtTarget}; cancel
 * suppresses the vanilla impulse. See EntityKnockbackByEntityEventHelper for shared logic.
 */
@Mixin(Mob.class)
public abstract class MobKnockbackMixin {

    @WrapOperation(
            method = "doHurtTarget(Lnet/minecraft/world/entity/Entity;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"
            )
    )
    private void paperarc$mobKnockback(LivingEntity target, double strength, double x, double z,
                                       Operation<Void> original, Mob attacker) {
        EntityKnockbackByEntityEventHelper.fire(target, strength, x, z, attacker, original);
    }
}
