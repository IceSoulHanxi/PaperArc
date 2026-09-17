package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.ProjectileBridge;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code Projectile.hasBeenShot} supplementary field. Field name
 * matches Paper exactly (no {@code paperarc$} prefix) so reflection on the NMS
 * class is ABI-compatible with Paper.
 */
@Mixin(Projectile.class)
public abstract class ProjectileFieldsMixin implements ProjectileBridge {

    @Unique
    public boolean hasBeenShot; // Paper

    @Override
    public boolean paper$hasBeenShot() {
        return this.hasBeenShot;
    }

    @Override
    public void paper$setHasBeenShot(boolean hasBeenShot) {
        this.hasBeenShot = hasBeenShot;
    }
}
