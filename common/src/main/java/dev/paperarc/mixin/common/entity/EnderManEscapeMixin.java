package dev.paperarc.mixin.common.entity;

import com.destroystokyo.paper.event.entity.EndermanEscapeEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.paperarc.bridge.PaperArcBridge;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's EndermanEscapeEvent.
 * <p>
 * Paper adds a synthetic {@code tryEscape(Reason)} helper called from three sites:
 * <ul>
 *   <li>{@code RUNAWAY} — appended to the daylight runaway condition in {@code customServerAiStep};
 *       we wrap the single {@code RandomSource.nextFloat()} call there and force the condition
 *       false (by returning 1.0F, since {@code 30 > (f - 0.4F) * 2.0F} always holds) when cancelled.</li>
 *   <li>{@code INDIRECT} — gates the 64-attempt teleport loop in {@code hurt} (the second
 *       {@code teleport()Z} invoke); we wrap that invoke and, once blocked, keep returning
 *       {@code false} so the loop drains and {@code hurt} still returns {@code flag1}, matching
 *       Paper's control flow.</li>
 * </ul>
 * The third site ({@code STARE}) lives in an inner class, see
 * {@link EnderManFreezeWhenLookedAtMixin}.
 */
@Mixin(EnderMan.class)
public abstract class EnderManEscapeMixin {

    @Unique
    private boolean paperarc$indirectFired;

    @Unique
    private boolean paperarc$indirectBlocked;

    @Unique
    private boolean paperarc$tryEscape(EndermanEscapeEvent.Reason reason) {
        EnderMan enderman = (EnderMan) (Object) this;
        return new EndermanEscapeEvent(
                PaperArcBridge.bukkitEntity(enderman), reason).callEvent();
    }

    // RUNAWAY: daylight runaway check in customServerAiStep. When our wrapper is
    // invoked, `f > 0.5F && canSeeSky` already evaluated true (short-circuit order).
    @WrapOperation(
            method = "customServerAiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextFloat()F")
    )
    private float paperarc$runaway(RandomSource instance, Operation<Float> original) {
        float roll = original.call(instance);
        float light = ((Entity) (Object) this).getLightLevelDependentMagicValue();
        if (roll * 30.0F < (light - 0.4F) * 2.0F && !this.paperarc$tryEscape(EndermanEscapeEvent.Reason.RUNAWAY)) {
            return 1.0F; // force the vanilla comparison false -> skip setTarget(null)/teleport
        }
        return roll;
    }

    @Inject(method = "hurt", at = @At("HEAD"))
    private void paperarc$resetIndirect(DamageSource source, float amount, CallbackInfo ci) {
        paperarc$indirectFired = false;
        paperarc$indirectBlocked = false;
    }

    // INDIRECT: ordinal 1 is the teleport call inside the 64-attempt escape loop
    // (ordinal 0 is vanilla's unrelated random-teleport-on-hurt).
    @WrapOperation(
            method = "hurt",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/EnderMan;teleport()Z", ordinal = 1)
    )
    private boolean paperarc$indirect(EnderMan instance, Operation<Boolean> original) {
        if (paperarc$indirectBlocked) {
            return false; // drain remaining loop iterations without teleporting
        }
        if (!paperarc$indirectFired) {
            paperarc$indirectFired = true;
            if (!this.paperarc$tryEscape(EndermanEscapeEvent.Reason.INDIRECT)) {
                paperarc$indirectBlocked = true;
                return false;
            }
        }
        return original.call(instance);
    }
}
