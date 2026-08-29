package com.ixnah.mc.paperarc.mixin.common.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.event.entity.PufferFishStateChangeEvent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pufferfish;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Implements Paper's PufferFishStateChangeEvent.
 * <p>
 * Paper patches {@link Pufferfish#tick()} so that each of the four puff-state
 * transitions fires a cancellable event before playing the blow sound and
 * applying the new state; when cancelled, the sound, the state change and the
 * inflate/deflate counter increment are all skipped.
 * <p>
 * We reproduce this without touching control flow: the four state changes are
 * exactly the four {@code makeSound} call sites inside {@code tick()}, so the
 * event is fired from a wrapper around {@code makeSound} (transition direction
 * derived from which sound is about to play), and cancellation suppresses the
 * subsequent {@code setPuffState} call plus that branch's counter increment
 * (the single PUTFIELD self-increment per counter).
 */
@Mixin(Pufferfish.class)
public abstract class PufferfishPuffStateMixin {

    @Unique
    private boolean paperarc$stateChangeCancelled;

    @Inject(method = "tick", at = @At("HEAD"))
    private void paperarc$resetCancelFlag(CallbackInfo ci) {
        this.paperarc$stateChangeCancelled = false;
    }

    @Unique
    private boolean paperarc$fireStateChangeEvent(boolean inflating) {
        Pufferfish fish = (Pufferfish) (Object) this;
        int newState = inflating ? fish.getPuffState() + 1 : fish.getPuffState() - 1;
        return new PufferFishStateChangeEvent(
            (org.bukkit.entity.PufferFish) PaperArcBridge.bukkitEntity(fish), newState).callEvent();
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/animal/Pufferfish;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private void paperarc$wrapMakeSound(Pufferfish instance, SoundEvent sound, float volume, float pitch, Operation<Void> original) {
        if (sound == SoundEvents.PUFFER_FISH_BLOW_UP) {
            if (!paperarc$fireStateChangeEvent(true)) {
                this.paperarc$stateChangeCancelled = true;
                return;
            }
        } else if (sound == SoundEvents.PUFFER_FISH_BLOW_OUT) {
            if (!paperarc$fireStateChangeEvent(false)) {
                this.paperarc$stateChangeCancelled = true;
                return;
            }
        }
        original.call(instance, sound, volume, pitch);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/animal/Pufferfish;setPuffState(I)V"))
    private void paperarc$wrapSetPuffState(Pufferfish instance, int state, Operation<Void> original) {
        if (!this.paperarc$stateChangeCancelled) {
            original.call(instance, state);
        }
    }

    @WrapOperation(method = "tick", at = @At(value = "FIELD",
        target = "Lnet/minecraft/world/entity/animal/Pufferfish;inflateCounter:I", opcode = Opcodes.PUTFIELD))
    private void paperarc$wrapInflateIncrement(Pufferfish instance, int newValue, Operation<Void> original) {
        if (!this.paperarc$stateChangeCancelled) {
            original.call(instance, newValue);
        }
    }

    @WrapOperation(method = "tick", at = @At(value = "FIELD",
        target = "Lnet/minecraft/world/entity/animal/Pufferfish;deflateTimer:I", opcode = Opcodes.PUTFIELD))
    private void paperarc$wrapDeflateIncrement(Pufferfish instance, int newValue, Operation<Void> original) {
        if (!this.paperarc$stateChangeCancelled) {
            original.call(instance, newValue);
        }
    }
}
