package com.ixnah.mc.paperarc.mixin.common.entity;

import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Duck accessors for BreedGoal's protected animal/partner fields, needed by
 * {@link TurtleBreedGoalMixin} (a subclass mixin cannot @Shadow parent fields).
 */
@Mixin(BreedGoal.class)
public interface BreedGoalAccessor {

    @Accessor("animal")
    Animal paperarc$getAnimal();

    @Accessor("partner")
    Animal paperarc$getPartner();
}
