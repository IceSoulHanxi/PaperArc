package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftSkeletonHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's SkeletonHorse trap API; direct delegation to the public vanilla
 * NMS methods {@code isTrap()}/{@code setTrap(boolean)}.
 */
@Mixin(CraftSkeletonHorse.class)
public abstract class CraftSkeletonHorseApiMixin {

    @Shadow
    public abstract SkeletonHorse getHandle();

    @Unique
    public boolean isTrap() {
        return getHandle().isTrap();
    }

    @Unique
    public void setTrap(boolean trap) {
        getHandle().setTrap(trap);
    }
}
