package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftAbstractHorse;
import org.bukkit.entity.Horse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Horse API missing from Arclight's CraftAbstractHorse.
 *
 * Mirrors Paper's Missing-Entity-API CraftAbstractHorse additions. The mouth/standing
 * flags live in the AbstractHorse-specific flag byte (not the Entity shared flags);
 * vanilla keeps {@code getFlag}/{@code setFlag} and both constants private, so they are
 * widened via AT (m_30647_ / m_30597_ / f_149496_ / f_149497_) and accessed directly —
 * no reflection.
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
        // Paper's NMS helper setForceStanding: raw write of the standing flag
        // (vanilla setStanding would additionally clear the grass-eating flag).
        getHandle().setFlag(AbstractHorse.FLAG_STANDING, rearing);
    }

    @Unique
    public boolean isEating() {
        return getHandle().getFlag(AbstractHorse.FLAG_OPEN_MOUTH);
    }

    @Unique
    public void setEating(boolean eating) {
        getHandle().setFlag(AbstractHorse.FLAG_OPEN_MOUTH, eating);
    }
}
