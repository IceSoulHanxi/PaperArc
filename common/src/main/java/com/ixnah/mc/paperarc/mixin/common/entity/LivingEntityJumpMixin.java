package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.LivingEntityBridge;
import com.destroystokyo.paper.event.entity.EntityJumpEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's EntityJumpEvent (Entity-Jump-API.patch) for LivingEntity.aiStep.
 *
 * Paper rewrites the jump branch:
 *   if (new EntityJumpEvent(getBukkitLivingEntity()).callEvent()) {
 *       this.jumpFromGround(); this.noJumpDelay = 10;
 *   } else { this.setJumping(false); }
 *
 * We wrap the single jumpFromGround() INVOKE in aiStep (bytecode-verified
 * 1.21.1: exactly one call site). On cancellation Paper skips jumpFromGround
 * and sets setJumping(false); noJumpDelay is still set by vanilla afterwards —
 * a known, accepted deviation shared with this implementation.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityJumpMixin implements LivingEntityBridge {

    @Invoker("setJumping")
    abstract void paperarc$setJumping(boolean jumping);

    @Override
    public void bridge$setJumping(boolean jumping) {
        this.paperarc$setJumping(jumping);
    }

    @WrapOperation(
            method = "aiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;jumpFromGround()V")
    )
    private void paperarc$entityJump(LivingEntity instance, Operation<Void> original) {
        EntityJumpEvent event = new EntityJumpEvent(
                PaperArcBridge.bukkitEntity(instance));
        if (event.callEvent()) {
            original.call(instance);
        } else {
            this.paperarc$setJumping(false);
        }
    }
}
