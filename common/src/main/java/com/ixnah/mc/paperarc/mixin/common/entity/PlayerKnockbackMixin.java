package com.ixnah.mc.paperarc.mixin.common.entity;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper 1.20.1's Implement-EntityKnockbackByEntityEvent-and-EntityPus.patch
 * (knockback half, Player side). Fires {@link EntityKnockbackByEntityEvent} at the two
 * {@code LivingEntity#knockback(DDD)} call sites inside {@code Player.attack}; cancel
 * suppresses the vanilla impulse. See EntityKnockbackByEntityEventHelper for the shared logic.
 */
@Mixin(Player.class)
public abstract class PlayerKnockbackMixin {

    @WrapOperation(
            method = "attack(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V",
                    ordinal = 0
            )
    )
    private void paperarc$playerKnockback1(LivingEntity target, double strength, double x, double z,
                                           Operation<Void> original, Player attacker) {
        EntityKnockbackByEntityEventHelper.fire(target, strength, x, z, attacker, original);
    }

    @WrapOperation(
            method = "attack(Lnet/minecraft/world/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V",
                    ordinal = 1
            )
    )
    private void paperarc$playerKnockback2(LivingEntity target, double strength, double x, double z,
                                           Operation<Void> original, Player attacker) {
        EntityKnockbackByEntityEventHelper.fire(target, strength, x, z, attacker, original);
    }
}
