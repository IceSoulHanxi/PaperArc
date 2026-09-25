package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.EntityKnockbackByEntityEventHelper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper 1.20.1's Implement-EntityKnockbackByEntityEvent-and-EntityPus.patch
 * (knockback half, Player side). Fires {@link com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent}
 * at the {@code LivingEntity#knockback(DDD)} call site inside {@code Player#causeExtraKnockback}; cancel
 * suppresses the vanilla impulse. Sweep attack knockback is in loader-specific mixins.
 */
@Mixin(Player.class)
public abstract class PlayerKnockbackMixin {

    @WrapOperation(
            method = "causeExtraKnockback(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/phys/Vec3;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"
            )
    )
    private void paperarc$playerKnockback(LivingEntity target, double strength, double x, double z,
                                          Operation<Void> original) {
        EntityKnockbackByEntityEventHelper.fire(target, strength, x, z, (Player) (Object) this, original);
    }
}
