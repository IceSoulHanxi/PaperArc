package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.animal.Rabbit;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftRabbit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Rabbit moreCarrotTicks API.
 *
 * Paper publicizes the private NMS field {@code Rabbit.moreCarrotTicks} via an
 * access transformer; a Craft-host mixin cannot shadow NMS privates, so the
 * field is accessed reflectively (mojmap runtime name: moreCarrotTicks).
 */
@Mixin(CraftRabbit.class)
public abstract class CraftRabbitApiMixin {

    @Shadow
    public abstract Rabbit getHandle();

    @Unique
    private static volatile java.lang.reflect.Field PAPERARC$MORE_CARROT_TICKS_FIELD;

    @Unique
    private static java.lang.reflect.Field paperarc$moreCarrotTicksField() {
        java.lang.reflect.Field f = PAPERARC$MORE_CARROT_TICKS_FIELD;
        if (f == null) {
            synchronized (CraftRabbitApiMixin.class) {
                if (PAPERARC$MORE_CARROT_TICKS_FIELD == null) {
                    try {
                        java.lang.reflect.Field resolved = Rabbit.class.getDeclaredField("moreCarrotTicks");
                        resolved.setAccessible(true);
                        PAPERARC$MORE_CARROT_TICKS_FIELD = resolved;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("NMS Rabbit.moreCarrotTicks field not found", e);
                    }
                }
                f = PAPERARC$MORE_CARROT_TICKS_FIELD;
            }
        }
        return f;
    }

    @Unique
    public int getMoreCarrotTicks() {
        try {
            return paperarc$moreCarrotTicksField().getInt(getHandle());
        } catch (IllegalAccessException e) {
            return 0;
        }
    }

    @Unique
    public void setMoreCarrotTicks(int ticks) {
        try {
            paperarc$moreCarrotTicksField().setInt(getHandle(), ticks);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to set NMS Rabbit.moreCarrotTicks", e);
        }
    }
}
