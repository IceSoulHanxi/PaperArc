package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.monster.Ghast;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftGhast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Ghast explosion-power API.
 *
 * Vanilla NMS only exposes {@code getExplosionPower()}; the backing
 * {@code explosionPower} field is private (Paper publicizes a setter via AT), so
 * the setter writes the field reflectively after Paper's 0..127 range check.
 */
@Mixin(CraftGhast.class)
public abstract class CraftGhastApiMixin {

    @Shadow
    public abstract Ghast getHandle();

    @Unique
    private static volatile java.lang.reflect.Field PAPERARC$EXPLOSION_POWER_FIELD;

    @Unique
    private static java.lang.reflect.Field paperarc$explosionPowerField() {
        java.lang.reflect.Field f = PAPERARC$EXPLOSION_POWER_FIELD;
        if (f == null) {
            synchronized (CraftGhastApiMixin.class) {
                if (PAPERARC$EXPLOSION_POWER_FIELD == null) {
                    try {
                        java.lang.reflect.Field resolved = Ghast.class.getDeclaredField("explosionPower");
                        resolved.setAccessible(true);
                        PAPERARC$EXPLOSION_POWER_FIELD = resolved;
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException("NMS Ghast.explosionPower field not found", e);
                    }
                }
                f = PAPERARC$EXPLOSION_POWER_FIELD;
            }
        }
        return f;
    }

    @Unique
    public int getExplosionPower() {
        return getHandle().getExplosionPower();
    }

    @Unique
    public void setExplosionPower(int explosionPower) {
        com.google.common.base.Preconditions.checkArgument(
            explosionPower >= 0 && explosionPower <= 127,
            "The explosion power has to be between 0 and 127");
        try {
            paperarc$explosionPowerField().setInt(getHandle(), explosionPower);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to set NMS Ghast.explosionPower", e);
        }
    }
}
