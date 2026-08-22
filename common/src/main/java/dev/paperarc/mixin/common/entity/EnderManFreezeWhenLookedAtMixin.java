package dev.paperarc.mixin.common.entity;

import com.destroystokyo.paper.event.entity.EndermanEscapeEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.paperarc.bridge.PaperArcBridge;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * STARE site of Paper's EndermanEscapeEvent: the teleport inside
 * {@code EnderMan.EndermanFreezeWhenLookedAt.tick()}. Cancelling the event simply
 * skips the wrapped {@code EnderMan.teleport()} call.
 */
@Mixin(targets = "net.minecraft.world.entity.monster.EnderMan$EndermanFreezeWhenLookedAt")
public abstract class EnderManFreezeWhenLookedAtMixin {

    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/EnderMan;teleport()Z")
    )
    private boolean paperarc$stareEscape(EnderMan enderman, Operation<Boolean> original) {
        if (!new EndermanEscapeEvent(
                PaperArcBridge.bukkitEntity(enderman),
                EndermanEscapeEvent.Reason.STARE).callEvent()) {
            return false;
        }
        return original.call(enderman);
    }
}
