package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.EnderMan;
import org.bukkit.craftbukkit.v.entity.CraftEnderman;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Adds Paper's Enderman API (Missing-Entity-API + Enderman.teleportRandomly).
 *
 * Vanilla NMS lacks {@code setCreepy}/{@code setHasBeenStaredAt}; Paper implements
 * them as {@code entityData.set(DATA_*, v)} with private-static accessors fetched
 * reflectively. {@code teleportRandomly} delegates to the protected vanilla
 * {@code EnderMan#teleport()}, invoked reflectively (mixin class is not a subtype,
 * so direct access would not compile).
 */
@Mixin(CraftEnderman.class)
public abstract class CraftEndermanApiMixin {

    @Shadow
    public abstract EnderMan getHandle();

    @Unique
    private static volatile EntityDataAccessor<Boolean> PAPERARC$DATA_CREEPY;
    @Unique
    private static volatile EntityDataAccessor<Boolean> PAPERARC$DATA_STARED_AT;

    @Unique
    private static EntityDataAccessor<Boolean> paperarc$dataCreepy() {
        EntityDataAccessor<Boolean> acc = PAPERARC$DATA_CREEPY;
        if (acc == null) {
            try {
                Field f = EnderMan.class.getDeclaredField("DATA_CREEPY");
                f.setAccessible(true);
                acc = (EntityDataAccessor<Boolean>) f.get(null);
                PAPERARC$DATA_CREEPY = acc;
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("NMS EnderMan.DATA_CREEPY not found", e);
            }
        }
        return acc;
    }

    @Unique
    private static EntityDataAccessor<Boolean> paperarc$dataStaredAt() {
        EntityDataAccessor<Boolean> acc = PAPERARC$DATA_STARED_AT;
        if (acc == null) {
            try {
                Field f = EnderMan.class.getDeclaredField("DATA_STARED_AT");
                f.setAccessible(true);
                acc = (EntityDataAccessor<Boolean>) f.get(null);
                PAPERARC$DATA_STARED_AT = acc;
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("NMS EnderMan.DATA_STARED_AT not found", e);
            }
        }
        return acc;
    }

    @Unique
    public boolean isScreaming() {
        return getHandle().isCreepy();
    }

    @Unique
    public void setScreaming(boolean screaming) {
        getHandle().getEntityData().set(paperarc$dataCreepy(), screaming);
    }

    @Unique
    public boolean hasBeenStaredAt() {
        return getHandle().hasBeenStaredAt();
    }

    @Unique
    public void setHasBeenStaredAt(boolean hasBeenStaredAt) {
        getHandle().getEntityData().set(paperarc$dataStaredAt(), hasBeenStaredAt);
    }

    @Unique
    public boolean teleportRandomly() {
        try {
            Method m = EnderMan.class.getDeclaredMethod("teleport");
            m.setAccessible(true);
            return (Boolean) m.invoke(getHandle());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("NMS EnderMan.teleport() not found", e);
        }
    }
}
