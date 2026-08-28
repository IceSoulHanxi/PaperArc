package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.monster.Endermite;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEndermite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Endermite lifetime API.
 *
 * Paper publicizes the private NMS field {@code Endermite.life} via an access
 * transformer; a Craft-host mixin cannot shadow NMS privates, so the field is
 * accessed reflectively (mojmap runtime name: life).
 */
@Mixin(CraftEndermite.class)
public abstract class CraftEndermiteApiMixin {

    @Shadow
    public abstract Endermite getHandle();

    @Unique
    private static volatile java.lang.reflect.Field PAPERARC$LIFE_FIELD;

    @Unique
    private static java.lang.reflect.Field paperarc$lifeField() {
        java.lang.reflect.Field f = PAPERARC$LIFE_FIELD;
        if (f == null) {
            synchronized (CraftEndermiteApiMixin.class) {
                if (PAPERARC$LIFE_FIELD == null) {
                    try {
                        java.lang.reflect.Field resolved = Endermite.class.getDeclaredField("life");
                        resolved.setAccessible(true);
                        PAPERARC$LIFE_FIELD = resolved;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("NMS Endermite.life field not found", e);
                    }
                }
                f = PAPERARC$LIFE_FIELD;
            }
        }
        return f;
    }

    @Unique
    public int getLifetimeTicks() {
        try {
            return paperarc$lifeField().getInt(getHandle());
        } catch (IllegalAccessException e) {
            return 0;
        }
    }

    @Unique
    public void setLifetimeTicks(int ticks) {
        try {
            paperarc$lifeField().setInt(getHandle(), ticks);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to set NMS Endermite.life", e);
        }
    }
}
