package dev.paperarc.mixin.fabric.entity;

import dev.paperarc.bridge.FertilizeEggState;
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
            at = @At(value = "INVOKE", target = "Lnet/minecraft/class_8153;method_49794(Lnet/minecraft/class_3218;Lnet/minecraft/class_1429;Lnet/minecraft/class_1296;)V",
                    remap = false)
    )
    private void paperarc$fertilize(net.minecraft.world.entity.animal.sniffer.Sniffer instance, ServerLevel level, Animal other, AgeableMob child,
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
