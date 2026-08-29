package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.FertilizeEggState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.frog.Frog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's EntityFertilizeEggEvent for Frog#spawnChildFromBreeding.
 * Wraps the finalizeSpawnChildFromBreeding call: cancelled events abort the
 * whole method (love already reset by {@link FertilizeEggState#call}), and
 * otherwise the event experience is threaded into the orb created inside
 * Animal#finalizeSpawnChildFromBreeding via {@link FertilizeEggState}.
 */
@Mixin(Frog.class)
public abstract class FrogFertilizeMixin {

    @WrapOperation(
            method = "spawnChildFromBreeding",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/frog/Frog;finalizeSpawnChildFromBreeding(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/Animal;Lnet/minecraft/world/entity/AgeableMob;)V")
    )
    private void paperarc$fertilize(Frog instance, ServerLevel level, Animal other, net.minecraft.world.entity.AgeableMob child,
                                    Operation<Void> original) {
        var event = FertilizeEggState.call(instance, other);
        if (event.isCancelled()) {
            return;
        }
        FertilizeEggState.PENDING_EXPERIENCE.set(event.getExperience());
        try {
            original.call(instance, level, other, child);
        } finally {
            FertilizeEggState.PENDING_EXPERIENCE.remove();
        }
    }
}
