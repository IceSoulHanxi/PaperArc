package dev.paperarc.mixin.common.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.Turtle;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.entity.CraftTurtle;
import org.bukkit.craftbukkit.v.util.CraftLocation;
import dev.paperarc.bridge.craft.CraftEntityBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Turtle-API to {@link CraftTurtle}.
 *
 * <p>{@code Turtle.getHomePos()}, {@code Turtle.isGoingHome()} and
 * {@code Turtle.setHasEgg(boolean)} are package-private in vanilla 1.21.1, so
 * they are reached via cached reflective lookups (mojmap runtime names); the
 * remaining calls use the public NMS accessors.</p>
 */
@Mixin(CraftTurtle.clas    @Shadow
    public abstract Turtle getHandle();

    @Unique
    private static volatile java.lang.reflect.Method PAPERARC$GET_HOME_POS;

    @Unique
    private static volatile java.lang.reflect.Method PAPERARC$IS_GOING_HOME;

    @Unique
    private static volatile java.lang.reflect.Method PAPERARC$SET_HAS_EGG;

    @Unique
    private static java.lang.reflect.Method paperarc$method(String name, Class<?>... params) {
        try {
            java.lang.reflect.Method resolved = Turtle.class.getDeclaredMethod(name, params);
            resolved.setAccessible(true);
            return resolved;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS Turtle." + name + " not found", e);
        }
    }

    @Unique
    private static java.lang.reflect.Method paperarc$getHomePos() {
        java.lang.reflect.Method m = PAPERARC$GET_HOME_POS;
        if (m == null) {
            synchronized (CraftTurtleApiMixin.class) {
                if (PAPERARC$GET_HOME_POS == null) {
                    PAPERARC$GET_HOME_POS = paperarc$method("getHomePos");
                }
                m = PAPERARC$GET_HOME_POS;
            }
        }
        return m;
    }

    @Unique
    private static java.lang.reflect.Method paperarc$isGoingHome() {
        java.lang.reflect.Method m = PAPERARC$IS_GOING_HOME;
        if (m == null) {
            synchronized (CraftTurtleApiMixin.class) {
                if (PAPERARC$IS_GOING_HOME == null) {
                    PAPERARC$IS_GOING_HOME = paperarc$method("isGoingHome");
                }
                m = PAPERARC$IS_GOING_HOME;
            }
        }
        return m;
    }

    @Unique
    private static java.lang.reflect.Method paperarc$setHasEgg() {
        java.lang.reflect.Method m = PAPERARC$SET_HAS_EGG;
        if (m == null) {
            synchronized (CraftTurtleApiMixin.class) {
                if (PAPERARC$SET_HAS_EGG == null) {
                    PAPERARC$SET_HAS_EGG = paperarc$method("setHasEgg", boolean.class);
                }
                m = PAPERARC$SET_HAS_EGG;
            }
        }
        return m;
    }

    @Unique
    public Location getHome() {
        BlockPos pos;
        try {
            pos = (BlockPos) paperarc$getHomePos().invoke(getHandle());
        } catch (java.lang.reflect.InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to invoke NMS Turtle.getHomePos()", e);
        }
        return CraftLocation.toBukkit(pos, getHandle().level());
    }

    @Unique
    public void setHome(Location location) {
        getHandle().setHomePos(CraftLocation.toBlockPosition(location));
    }

    @Unique
    public boolean isGoingHome() {
        try {
            return (Boolean) paperarc$isGoingHome().invoke(getHandle());
        } catch (java.lang.reflect.InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to invoke NMS Turtle.isGoingHome()", e);
        }
    }

    @Unique
    public boolean isDigging() {
        return getHandle().isLayingEgg();
    }

    @Unique
    public void setHasEgg(boolean hasEgg) {
        try {
            paperarc$setHasEgg().invoke(getHandle(), hasEgg);
        } catch (java.lang.reflect.InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException("Failed to invoke NMS Turtle.setHasEgg(boolean)", e);
        }
    }
}
