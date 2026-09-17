package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.monster.Ghast;
import org.bukkit.craftbukkit.v.entity.CraftGhast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Ghast explosion-power API.
 *
 * Vanilla NMS only exposes {@code getExplosionPower()}; the backing
 * {@code explosionPower} field is private (Paper publicizes a setter via AT) and
 * is opened by {@code paperarc.accesswidener}, so the setter writes it directly
 * after Paper's 0..127 range check.
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
