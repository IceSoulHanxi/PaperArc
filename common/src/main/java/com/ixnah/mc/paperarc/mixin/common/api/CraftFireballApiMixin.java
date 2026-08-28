package com.ixnah.mc.paperarc.mixin.common.api;

import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftFireball;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Fireball power API.
 *
 * Paper delegates to its own acceleration API. Since 1.21 the NMS projectile
 * carries its thrust as a plain motion vector (constructors normalize the
 * requested direction and scale by accelerationPower via
 * assignDirectionalMovement -> setDeltaMovement), so power is exposed through
 * getDeltaMovement()/setDeltaMovement().
 */
@Mixin(CraftFireball.class)
public abstract class CraftFireballApiMixin {

    @Shadow
    public abstract AbstractHurtingProjectile getHandle();

    @Unique
    public Vector getPower() {
        Vec3 delta = getHandle().getDeltaMovement();
        return new Vector(delta.x, delta.y, delta.z);
    }

    @Unique
    public void setPower(Vector power) {
        if (power == null) {
            throw new IllegalArgumentException("power cannot be null");
        }
        getHandle().setDeltaMovement(new Vec3(power.getX(), power.getY(), power.getZ()));
    }
}
