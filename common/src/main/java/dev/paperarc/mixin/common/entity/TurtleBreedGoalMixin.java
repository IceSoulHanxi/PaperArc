package dev.paperarc.mixin.common.entity;

import dev.paperarc.bridge.FertilizeEggState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.papermc.paper.event.entity.EntityFertilizeEggEvent;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's EntityFertilizeEggEvent for Turtle breeding.
 * Paper fires the event at the head of {@code Turtle.TurtleBreedGoal#breed}
 * and uses its experience for the orb. The goal is a private inner class
 * (string target) and its animal/partner fields live in {@link BreedGoal}
 * (parent class, not shadowable from a subclass mixin), so both are read via
 * the {@link BreedGoalAccessor} duck interface.
 */
@Mixin(targets = "net.minecraft.world.entity.animal.Turtle$TurtleBreedGoal")
public abstract class TurtleBreedGoalMixin {

    @Unique
    private int paperarc$fertilizeExperience = -1;

    @Inject(method = "breed", at = @At("HEAD"), cancellable = true)
    private void paperarc$fertilize(CallbackInfo ci) {
        BreedGoalAccessor self = (BreedGoalAccessor) this;
        EntityFertilizeEggEvent event = FertilizeEggState.call(self.paperarc$getAnimal(), self.paperarc$getPartner());
        if (event.isCancelled()) {
            ci.cancel();
            return;
        }
        this.paperarc$fertilizeExperience = event.getExperience();
    }

    @WrapOperation(
            method = "breed",
            at = @At(value = "NEW", target = "Lnet/minecraft/world/entity/experience/ExperienceOrb;<init>(Lnet/minecraft/world/level/Level;DDDI)V")
    )
    private ExperienceOrb paperarc$fertilizeXp(Level level, double x, double y, double z, int amount,
                                               Operation<ExperienceOrb> original) {
        int pending = this.paperarc$fertilizeExperience;
        if (pending < 0) {
            return original.call(level, x, y, z, amount);
        }
        return original.call(level, x, y, z, pending);
    }
}
