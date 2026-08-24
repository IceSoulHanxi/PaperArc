package dev.paperarc.mixin.common.api;

import net.minecraft.world.entity.animal.Panda;
import org.bukkit.craftbukkit.v.entity.CraftPanda;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's Missing-Entity-API (Panda API) additions on
 * {@link CraftPanda}.
 *
 * Sneeze/unhappy counters map to public NMS accessors. The eat counter is
 * private in vanilla NMS ({@code getEatCounter}/{@code setEatCounter}), so
 * those two are invoked reflectively; Paper reaches them via its own access
 * widener, which a Craft-host mixin cannot replicate.
 */
@Mixin(CraftPanda.class)
public abstract class CraftPandaApiMixin {

    @Shadow
    public abstract Panda getHandle();

    @Unique
    private static volatile java.lang.reflect.Method PAPERARC$GET_EAT_COUNTER;

    @Unique
    private static volatile java.lang.reflect.Method PAPERARC$SET_EAT_COUNTER;

    @Unique
    private static void paperarc$resolveEatCounterMethods() {
        if (PAPERARC$GET_EAT_COUNTER == null || PAPERARC$SET_EAT_COUNTER == null) {
            synchronized (CraftPandaApiMixin.class) {
                if (PAPERARC$GET_EAT_COUNTER == null || PAPERARC$SET_EAT_COUNTER == null) {
                    try {
                        java.lang.reflect.Method getter = Panda.class.getDeclaredMethod("getEatCounter");
                        getter.setAccessible(true);
                        java.lang.reflect.Method setter = Panda.class.getDeclaredMethod("setEatCounter", int.class);
                        setter.setAccessible(true);
                        PAPERARC$GET_EAT_COUNTER = getter;
                        PAPERARC$SET_EAT_COUNTER = setter;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("NMS Panda eat counter methods not found", e);
                    }
                }
            }
        }
    }

    // Paper start - Missing Entity API
    @Unique
    public int getSneezeTicks() {
        return this.getHandle().getSneezeCounter();
    }

    @Unique
    public void setSneezeTicks(int ticks) {
        this.getHandle().setSneezeCounter(ticks);
    }

    @Unique
    public int getEatingTicks() {
        paperarc$resolveEatCounterMethods();
        try {
            return (Integer) PAPERARC$GET_EAT_COUNTER.invoke(this.getHandle());
        } catch (java.lang.reflect.InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to read NMS Panda eat counter", e);
        }
    }

    @Unique
    public void setEatingTicks(int ticks) {
        paperarc$resolveEatCounterMethods();
        try {
            PAPERARC$SET_EAT_COUNTER.invoke(this.getHandle(), ticks);
        } catch (java.lang.reflect.InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to write NMS Panda eat counter", e);
        }
    }

    @Unique
    public void setUnhappyTicks(int ticks) {
        this.getHandle().setUnhappyCounter(ticks);
    }

    @Unique
    public org.bukkit.entity.Panda.Gene getCombinedGene() {
        // Paper: CraftPanda.fromNms(this.getHandle().getVariant()) — gene enums
        // share identical constant names, so map by name.
        return org.bukkit.entity.Panda.Gene.valueOf(this.getHandle().getVariant().name());
    }
    // Paper end - Missing Entity API
}
