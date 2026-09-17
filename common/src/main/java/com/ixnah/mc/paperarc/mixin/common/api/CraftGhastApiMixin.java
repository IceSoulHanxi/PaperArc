package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.monster.Ghast;
import org.bukkit.craftbukkit.v.entity.CraftGhast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Ghast explosion-power API.
 *
 * <p>Vanilla NMS only exposes {@code getExplosionPower()}; the backing
 * {@code explosionPower} field is private (Paper publicizes it via AT). The same AT
 * entry exists here (f_32722_ in META-INF/accesstransformer.cfg), so the setter writes
 * the field directly — a string-name reflective lookup would fail on the srg runtime.
 */
@Mixin(CraftGhast.class)
public abstract class CraftGhastApiMixin {

    @Shadow
    public abstract Ghast getHandle();

    @Unique
    public int getExplosionPower() {
        return getHandle().getExplosionPower();
    }

    @Unique
    public void setExplosionPower(int explosionPower) {
        com.google.common.base.Preconditions.checkArgument(
            explosionPower >= 0 && explosionPower <= 127,
            "The explosion power has to be between 0 and 127");
        getHandle().explosionPower = explosionPower;
    }
}
