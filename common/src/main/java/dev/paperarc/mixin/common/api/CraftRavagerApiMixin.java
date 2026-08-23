package dev.paperarc.mixin.common.api;

import net.minecraft.world.entity.monster.Ravager;
import org.bukkit.craftbukkit.v.entity.CraftRavager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's Missing-Entity-API (Missing Entity Behavior) additions on
 * {@link CraftRavager}.
 *
 * Getters delegate to public NMS accessors; the tick fields
 * ({@code attackTick}/{@code stunnedTick}/{@code roarTick}) are private in
 * vanilla NMS — Paper writes them directly via an access widener, which a
 * Craft-host mixin cannot replicate, so they are set reflectively.
 */
@Mixin(CraftRavager.class)
public abstract class CraftRavagerApiMixin {

    @Shadow
    public abstract Ravager getHandle();

    @Unique
    private static volatile java.lang.reflect.Field PAPERARC$ATTACK_TICK;

    @Unique
    private static volatile java.lang.reflect.Field PAPERARC$STUNNED_TICK;

    @Unique
    private static volatile java.lang.reflect.Field PAPERARC$ROAR_TICK;

    @Unique
    private static java.lang.reflect.Field paperarc$resolveField(String name) {
        try {
            java.lang.reflect.Field f = Ravager.class.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS Ravager." + name + " field not found", e);
        }
    }

    @Unique
    private static java.lang.reflect.Field paperarc$attackTickField() {
        java.lang.reflect.Field f = PAPERARC$ATTACK_TICK;
        if (f == null) {
            synchronized (CraftRavagerApiMixin.class) {
                if ((f = PAPERARC$ATTACK_TICK) == null) {
                    f = PAPERARC$ATTACK_TICK = paperarc$resolveField("attackTick");
                }
            }
        }
        return f;
    }

    @Unique
    private static java.lang.reflect.Field paperarc$stunnedTickField() {
        java.lang.reflect.Field f = PAPERARC$STUNNED_TICK;
        if (f == null) {
            synchronized (CraftRavagerApiMixin.class) {
                if ((f = PAPERARC$STUNNED_TICK) == null) {
                    f = PAPERARC$STUNNED_TICK = paperarc$resolveField("stunnedTick");
                }
            }
        }
        return f;
    }

    @Unique
    private static java.lang.reflect.Field paperarc$roarTickField() {
        java.lang.reflect.Field f = PAPERARC$ROAR_TICK;
        if (f == null) {
            synchronized (CraftRavagerApiMixin.class) {
                if ((f = PAPERARC$ROAR_TICK) == null) {
                    f = PAPERARC$ROAR_TICK = paperarc$resolveField("roarTick");
                }
            }
        }
        return f;
    }

    // Paper start - Missing Entity Behavior
    @Unique
    public int getAttackTicks() {
        return this.getHandle().getAttackTick();
    }

    @Unique
    public void setAttackTicks(int ticks) {
        try {
            paperarc$attackTickField().setInt(this.getHandle(), ticks);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to write NMS Ravager.attackTick", e);
        }
    }

    @Unique
    public int getStunnedTicks() {
        return this.getHandle().getStunnedTick();
    }

    @Unique
    public void setStunnedTicks(int ticks) {
        try {
            paperarc$stunnedTickField().setInt(this.getHandle(), ticks);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to write NMS Ravager.stunnedTick", e);
        }
    }

    @Unique
    public int getRoarTicks() {
        return this.getHandle().getRoarTick();
    }

    @Unique
    public void setRoarTicks(int ticks) {
        try {
            paperarc$roarTickField().setInt(this.getHandle(), ticks);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to write NMS Ravager.roarTick", e);
        }
    }
    // Paper end - Missing Entity Behavior
}
