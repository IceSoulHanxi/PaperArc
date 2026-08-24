package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.animal.Chicken;
import org.bukkit.craftbukkit.v.entity.CraftChicken;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's Missing-Entity-API additions on {@link CraftChicken}:
 * chicken-jockey flag and egg-lay time, all backed by public NMS fields/
 * methods ({@code isChickenJockey}, {@code setChickenJockey},
 * {@code eggTime}).
 */
@Mixin(CraftChicken.class)
public abstract class CraftChickenApiMixin {

    @Shadow
    public abstract Chicken getHandle();

    // Paper start - Missing Entity API
    @Unique
    public boolean isChickenJockey() {
        return this.getHandle().isChickenJockey();
    }

    @Unique
    public void setIsChickenJockey(boolean isChickenJockey) {
        this.getHandle().setChickenJockey(isChickenJockey);
    }

    @Unique
    public int getEggLayTime() {
        return this.getHandle().eggTime;
    }

    @Unique
    public void setEggLayTime(int eggLayTime) {
        this.getHandle().eggTime = eggLayTime;
    }
    // Paper end - Missing Entity API
}
