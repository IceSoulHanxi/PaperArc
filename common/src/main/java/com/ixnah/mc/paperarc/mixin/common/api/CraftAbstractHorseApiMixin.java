package com.ixnah.mc.paperarc.mixin.common.api;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
 * Those wrappers do not exist in the runtime NMS and the flag constants are
 * private static finals, so the flags are reached reflectively here.
 */
@Mixin(CraftAbstractHorse.class)
public abstract class CraftAbstractHorseApiMixin {

    @Shadow
    public abstract AbstractHorse getHandle();

    @Unique
    private static volatile Method PAPERARC$GET_SHARED_FLAG;
    @Unique
    private static volatile Method PAPERARC$SET_SHARED_FLAG;
    @Unique
    private static volatile Field PAPERARC$FLAG_STANDING;
    @Unique
    private static volatile Field PAPERARC$FLAG_OPEN_MOUTH;

    @Unique
    private static Method paperarc$getSharedFlagMethod() {
        Method m = PAPERARC$GET_SHARED_FLAG;
        if (m == null) {
            synchronized (CraftAbstractHorseApiMixin.class) {
                if (PAPERARC$GET_SHARED_FLAG == null) {
                    try {
                        Method resolved = net.minecraft.world.entity.Entity.class
                                .getDeclaredMethod("getSharedFlag", int.class);
                        resolved.setAccessible(true);
                        PAPERARC$GET_SHARED_FLAG = resolved;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("NMS Entity.getSharedFlag(int) not found", e);
                    }
                }
                m = PAPERARC$GET_SHARED_FLAG;
            }
        }
        return m;
    }

    @Unique
    private static Method paperarc$setSharedFlagMethod() {
        Method m = PAPERARC$SET_SHARED_FLAG;
        if (m == null) {
            synchronized (CraftAbstractHorseApiMixin.class) {
                if (PAPERARC$SET_SHARED_FLAG == null) {
                    try {
                        Method resolved = net.minecraft.world.entity.Entity.class
                                .getDeclaredMethod("setSharedFlag", int.class, boolean.class);
                        resolved.setAccessible(true);
                        PAPERARC$SET_SHARED_FLAG = resolved;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("NMS Entity.setSharedFlag(int, boolean) not found", e);
                    }
                }
                m = PAPERARC$SET_SHARED_FLAG;
            }
        }
        return m;
    }

    @Unique
    private static int paperarc$flagConstant(String name) {
        Field cached = name.equals("FLAG_STANDING") ? PAPERARC$FLAG_STANDING : PAPERARC$FLAG_OPEN_MOUTH;
        if (cached != null) {
            try {
                return cached.getInt(null);
            } catch (IllegalAccessException ignored) {
                // fall through and re-resolve
            }
        }
        synchronized (CraftAbstractHorseApiMixin.class) {
            try {
                Field resolved = AbstractHorse.class.getDeclaredField(name);
                resolved.setAccessible(true);
                if (name.equals("FLAG_STANDING")) {
                    PAPERARC$FLAG_STANDING = resolved;
                } else {
                    PAPERARC$FLAG_OPEN_MOUTH = resolved;
                }
                return resolved.getInt(null);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("NMS AbstractHorse." + name + " flag constant not found", e);
            }
        }
    }

    @Unique
    private boolean paperarc$isMouthOpen(AbstractHorse handle) {
        try {
            return (Boolean) paperarc$getSharedFlagMethod()
                    .invoke(handle, paperarc$flagConstant("FLAG_OPEN_MOUTH"));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to read AbstractHorse mouth-open flag", e);
        }
    }

    @Unique
    private void paperarc$setMouthOpen(AbstractHorse handle, boolean open) {
        try {
            paperarc$setSharedFlagMethod()
                    .invoke(handle, paperarc$flagConstant("FLAG_OPEN_MOUTH"), open);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to write AbstractHorse mouth-open flag", e);
        }
    }

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
        try {
            paperarc$setSharedFlagMethod()
                    .invoke(getHandle(), paperarc$flagConstant("FLAG_STANDING"), rearing);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to write AbstractHorse standing flag", e);
        }
    }

    @Unique
    public boolean isEating() {
        return paperarc$isMouthOpen(getHandle());
    }

    @Unique
    public void setEating(boolean eating) {
        paperarc$setMouthOpen(getHandle(), eating);
    }
}
