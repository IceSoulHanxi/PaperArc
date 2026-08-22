package dev.paperarc.mixin.common.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Port of Paper's EntityFertilizeEggEvent for Sniffer#spawnChildFromBreeding.
 * Same approach as {@link FrogFertilizeMixin}: cancel drops the whole method
 * (including the egg item drop), otherwise the event experience threads into
 * the orb created inside Animal#finalizeSpawnChildFromBreeding.
 */
@Mixin(Sniffer.class)
public abstract class SnifferFertilizeMixin {

    @WrapOperation(
            method = "spawnChildFromBreeding",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Animal;finalizeSpawnChildFromBreeding(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/Animal;Lnet/minecraft/world/entity/AgeableMob;)V")
    )
    private void paperarc$fertilize(Animal instance, ServerLevel level, Animal other, AgeableMob child,
                                    Operation<Void> original) {
        var event = FertilizeEggState.call((Animal) (Object) this, other);
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
