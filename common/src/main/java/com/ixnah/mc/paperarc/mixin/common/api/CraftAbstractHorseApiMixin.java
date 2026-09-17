package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.bukkit.craftbukkit.v.entity.CraftAbstractHorse;
import org.bukkit.entity.Horse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Horse API missing from Arclight's CraftAbstractHorse.
 *
 * Paper's CB implementation delegates to {@code AbstractHorse#isMouthOpen()} /
 * {@code setForceStanding()}, both added by Paper as thin wrappers over the
 * vanilla synched-data flags ({@code getSharedFlag}/{@code setSharedFlag}).
 * Those wrappers do not exist in the runtime NMS; the flag constants and the two
 * protected {@code get/setSharedFlag} helpers are opened by
 * {@code paperarc.accesswidener} and used directly.
 */
@Mixin(CraftAbstractHorse.class)
public abstract class CraftAbstractHorseApiMixin {

    @Shadow
    public abstract AbstractHorse getHandle();

    /**
     * No Paper CB reference implements this (Spigot's counterpart setVariant is an
     * UnsupportedOperationException stub); the variant is derived from the NMS type.
     */
    @Unique
    public Horse.Variant getVariant() {
        AbstractHorse handle = getHandle();
        if (handle instanceof net.minecraft.world.entity.animal.horse.SkeletonHorse) {
            return Horse.Variant.SKELETON_HORSE;
        }
        if (handle instanceof net.minecraft.world.entity.animal.horse.ZombieHorse) {
            return Horse.Variant.UNDEAD_HORSE;
        }
        if (handle instanceof net.minecraft.world.entity.animal.horse.Llama) {
            return Horse.Variant.LLAMA;
        }
        if (handle instanceof net.minecraft.world.entity.animal.camel.Camel) {
            return Horse.Variant.CAMEL;
        }
        if (handle instanceof net.minecraft.world.entity.animal.horse.Mule) {
            return Horse.Variant.MULE;
        }
        if (handle instanceof net.minecraft.world.entity.animal.horse.Donkey) {
            return Horse.Variant.DONKEY;
        }
        return Horse.Variant.HORSE;
    }

    @Unique
    public boolean isEatingGrass() {
        return getHandle().isEating();
    }

    @Unique
    public void setEatingGrass(boolean eatingGrass) {
        getHandle().setEating(eatingGrass);
    }

    @Unique
    public boolean isRearing() {
        return getHandle().isStanding();
    }

    @Unique
    public void setRearing(boolean rearing) {
        // Mirror Paper's NMS helper setForceStanding: raw write of the standing flag
        // (vanilla setStanding would additionally clear the grass-eating flag).
        getHandle().setSharedFlag(AbstractHorse.FLAG_STANDING, rearing);
    }

    @Unique
    public boolean isEating() {
        return getHandle().getSharedFlag(AbstractHorse.FLAG_OPEN_MOUTH);
    }

    @Unique
    public void setEating(boolean eating) {
        getHandle().setSharedFlag(AbstractHorse.FLAG_OPEN_MOUTH, eating);
    }
}
