package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.FertilizeEggState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Experience plumbing for the EntityFertilizeEggEvent port.
 * When a fertilize handler stored an experience override (Frog/Sniffer paths),
 * the ExperienceOrb created by Animal#finalizeSpawnChildFromBreeding uses it
 * instead of the vanilla random amount. Arclight @Overwrites this method but
 * keeps the identical 5-arg ExperienceOrb constructor, so the wrap still hits.
 */
@Mixin(Animal.class)
public abstract class AnimalFinalizeSpawnMixin {

    @WrapOperation(
            method = "finalizeSpawnChildFromBreeding",
            at = @At(value = "NEW", target = "Lnet/minecraft/world/entity/ExperienceOrb;")
    )
    private ExperienceOrb paperarc$fertilizeXp(Level level, double x, double y, double z, int amount,
                                               Operation<ExperienceOrb> original) {
        Integer pending = FertilizeEggState.PENDING_EXPERIENCE.get();
        if (pending == null) {
            return original.call(level, x, y, z, amount);
        }
        return original.call(level, x, y, z, pending);
    }
}
