package dev.paperarc.mixin.common;

import com.destroystokyo.paper.event.entity.EntityJumpEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.paperarc.bridge.PaperArcBridge;
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
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;jumpFromGround()V")
    )
    private void paperarc$entityJump(Operation<Void> original) {
        EntityJumpEvent event = new EntityJumpEvent(
                PaperArcBridge.bukkitEntity((Entity) (Object) this));
        if (event.callEvent()) {
            original.call();
        } else {
            ((LivingEntityBridge) (Object) this).bridge$setJumping(false);
        }
    }
}
