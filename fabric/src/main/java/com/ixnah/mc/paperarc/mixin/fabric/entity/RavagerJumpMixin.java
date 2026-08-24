package com.ixnah.mc.paperarc.mixin.fabric.entity;

import com.ixnah.mc.paperarc.bridge.LivingEntityBridge;
import com.destroystokyo.paper.event.entity.EntityJumpEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ravager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's EntityJumpEvent for Ravager.aiStep. Same pattern as
 * {@link LivingEntityJumpMixin}. Bytecode-verified 1.21.1: Ravager.aiStep
 * contains exactly one jumpFromGround() call. Arclight's RavagerMixin only
 * redirects Level.destroyBlock inside aiStep — different call site, no conflict.
 */
@Mixin(Ravager.class)
public abstract class RavagerJumpMixin {

    @WrapOperation(
            method = "aiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1584;method_6043()V", remap = false)
    )
    private void paperarc$entityJump(Ravager instance, Operation<Void> original) {
        EntityJumpEvent event = new EntityJumpEvent(
                PaperArcBridge.bukkitEntity(instance));
        if (event.callEvent()) {
            original.call(instance);
        } else {
            ((LivingEntityBridge) instance).bridge$setJumping(false);
        }
    }
}
