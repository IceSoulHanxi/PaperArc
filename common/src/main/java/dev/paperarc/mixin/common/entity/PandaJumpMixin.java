package dev.paperarc.mixin.common.entity;

import dev.paperarc.bridge.LivingEntityBridge;
import com.destroystokyo.paper.event.entity.EntityJumpEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.paperarc.bridge.PaperArcBridge;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Panda;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's EntityJumpEvent for Panda.afterSneeze (baby pandas jumping).
 * Same pattern as {@link LivingEntityJumpMixin}; Paper patches the identical
 * branch here. Bytecode-verified 1.21.1: afterSneeze contains exactly one
 * jumpFromGround() call.
 */
@Mixin(Panda.class)
public abstract class PandaJumpMixin {

    @WrapOperation(
            method = "afterSneeze",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1440;method_6043()V", remap = false)
    )
    private void paperarc$entityJump(Panda instance, Operation<Void> original) {
        EntityJumpEvent event = new EntityJumpEvent(
                PaperArcBridge.bukkitEntity(instance));
        if (event.callEvent()) {
            original.call(instance);
        } else {
            ((LivingEntityBridge) instance).bridge$setJumping(false);
        }
    }
}
