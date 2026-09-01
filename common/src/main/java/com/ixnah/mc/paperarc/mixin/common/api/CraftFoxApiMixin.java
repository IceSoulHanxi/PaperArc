package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.animal.Fox;
import org.bukkit.craftbukkit.v.entity.CraftFox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's "more fox behavior API" to CraftFox.
 *
 * Delegates straight to NMS Fox flag accessors (mojmap names verified via javap):
 * interested -> setIsInterested/isInterested, leaping -> setIsPouncing/isPouncing,
 * defending -> setDefending/isDefending, faceplanted -> setFaceplanted.
 */
@Mixin(CraftFox.class)
public abstract class CraftFoxApiMixin {

    @Shadow
    public abstract Fox getHandle();

    @Unique
    public boolean isDefending() {
        return this.getHandle().isDefending();
    }

    @Unique
    public boolean isInterested() {
        return this.getHandle().isInterested();
    }

    @Unique
    public boolean isLeaping() {
        return this.getHandle().isPouncing();
    }

    @Unique
    public void setDefending(boolean defending) {
        this.getHandle().setDefending(defending);
    }

    @Unique
    public void setFaceplanted(boolean faceplanted) {
        this.getHandle().setFaceplanted(faceplanted);
    }

    @Unique
    public void setInterested(boolean interested) {
        this.getHandle().setIsInterested(interested);
    }

    @Unique
    public void setLeaping(boolean leaping) {
        this.getHandle().setIsPouncing(leaping);
    }
}
