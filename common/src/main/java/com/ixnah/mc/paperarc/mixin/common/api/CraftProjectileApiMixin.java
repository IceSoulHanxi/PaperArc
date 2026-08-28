package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import com.ixnah.mc.paperarc.bridge.ApiState;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftProjectile;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

/**
 * Port of Paper's More-Projectile-API additions on {@link CraftProjectile}.
 *
 * <p>Mappings to this codebase's NMS (mojmap 1.21.1 {@code Projectile}):
 * <ul>
 *   <li>{@code getOwnerUniqueId()} → private field {@code ownerUUID} (@Shadow).</li>
 *   <li>{@code hasLeftShooter()} / {@code setHasLeftShooter(boolean)} → private
 *       field {@code leftOwner} (@Shadow).</li>
 *   <li>{@code hasBeenShot()} / {@code setHasBeenShot(boolean)} → Paper keeps
 *       this flag in an NMS-patched field that vanilla 1.21.1 {@code Projectile}
 *       has no storage for, so it is kept in {@link ApiState} keyed by the NMS
 *       handle (side-map, default {@code false}).</li>
 *   <li>{@code canHitEntity(Entity)} → protected NMS
 *       {@code Projectile#canHitEntity(Entity)} via cached reflection.</li>
 *   <li>{@code hitEntity(...)} → protected NMS {@code Projectile#onHit(HitResult)}
 *       with an {@link EntityHitResult} targeting the hit entity, via cached
 *       reflection; guarded by the same removed-check Paper uses.</li>
 * </ul>
 */
@Mixin(CraftProjectile.class)
public abstract class CraftProjectileApiMixin {

    @Shadow
    public abstract Projectile getHandle();

    @Unique
    private static final String PAPERARC$KEY_HAS_BEEN_SHOT = "hasBeenShot";

    @Unique
    public boolean canHitEntity(org.bukkit.entity.Entity entity) {
        return this.getHandle().canHitEntity(((CraftEntity) entity).getHandle());
    }

    @Unique
    public UUID getOwnerUniqueId() {
        return this.getHandle().ownerUUID;
    }

    @Unique
    public boolean hasBeenShot() {
        return ApiState.get(this.getHandle(), PAPERARC$KEY_HAS_BEEN_SHOT, Boolean.FALSE);
    }

    @Unique
    public boolean hasLeftShooter() {
        return this.getHandle().leftOwner;
    }

    @Unique
    public void hitEntity(org.bukkit.entity.Entity entity) {
        Preconditions.checkState(!this.getHandle().isRemoved(),
            "Cannot hit entity on a removed projectile");
        this.getHandle()
            .onHit(new EntityHitResult(((CraftEntity) entity).getHandle()));
    }

    @Unique
    public void hitEntity(org.bukkit.entity.Entity entity, Vector vector) {
        // Paper passes the vector as the original-movement argument of
        // onHit(HitResult, Vec3); 1.21.1 NMS only has onHit(HitResult),
        // so the deflection hint cannot be applied and is ignored.
        this.hitEntity(entity);
    }

    @Unique
    public void setHasBeenShot(boolean hasBeenShot) {
        ApiState.put(this.getHandle(), PAPERARC$KEY_HAS_BEEN_SHOT, hasBeenShot);
    }

    @Unique
    public void setHasLeftShooter(boolean hasLeftShooter) {
        this.getHandle().leftOwner = hasLeftShooter;
    }
}
