package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.animal.Rabbit;
import org.bukkit.craftbukkit.v.entity.CraftRabbit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Rabbit moreCarrotTicks API.
 *
 * Paper publicizes the private NMS field {@code Rabbit.moreCarrotTicks} via an
 * access transformer; {@code paperarc.accesswidener} opens it the same way.
 */
@Mixin(CraftRabbit.class)
public abstract class CraftRabbitApiMixin {

    @Shadow
    public abstract Rabbit getHandle();

    @Unique
    public int getMoreCarrotTicks() {
        return getHandle().moreCarrotTicks;
    }

    @Unique
    public void setMoreCarrotTicks(int ticks) {
        getHandle().moreCarrotTicks = ticks;
    }
}
