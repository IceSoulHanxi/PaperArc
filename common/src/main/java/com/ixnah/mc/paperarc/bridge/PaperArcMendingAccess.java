package com.ixnah.mc.paperarc.bridge;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * MethodHandle accessors for {@code ExperienceOrb} members that Paper exposes
 * via an access transformer (AT) but Arclight keeps private:
 * the {@code value} field and the {@code xpToDurability}/{@code durabilityToXp}
 * helpers used by {@code Player#applyMending}.
 */
public final class PaperArcMendingAccess {

    public static final MethodHandle VALUE_FIELD;
    public static final MethodHandle XP_TO_DURABILITY;
    public static final MethodHandle DURABILITY_TO_XP;

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(
                    net.minecraft.world.entity.ExperienceOrb.class, lookup);
            VALUE_FIELD = privateLookup.findSetter(
                    net.minecraft.world.entity.ExperienceOrb.class, "value", int.class);
            XP_TO_DURABILITY = privateLookup.findVirtual(
                    net.minecraft.world.entity.ExperienceOrb.class, "xpToDurability",
                    MethodType.methodType(int.class, int.class));
            DURABILITY_TO_XP = privateLookup.findVirtual(
                    net.minecraft.world.entity.ExperienceOrb.class, "durabilityToXp",
                    MethodType.methodType(int.class, int.class));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to bind ExperienceOrb mending accessors", e);
        }
    }

    private PaperArcMendingAccess() {
    }
}
