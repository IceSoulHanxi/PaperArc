package dev.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import dev.paperarc.bridge.ApiState;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.craftbukkit.v.entity.CraftProjectile;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Method;
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

    @Shadow
    private UUID ownerUUID;

    @Shadow
    private boolean leftOwner;

    @Unique
    private static final String PAPERARC$KEY_HAS_BEEN_SHOT = "hasBeenShot";

    /**
     * Cached reflective handles into protected vanilla helpers:
     * [0] Projectile#canHitEntity(Entity),
     * [1] Projectile#onHit(HitResult).
     */
    @Unique
    private static volatile Method[] PAPERARC$PROJ_HELPERS;

    @Unique
    public boolean canHitEntity(org.bukkit.entity.Entity entity) {
        try {
            return (Boolean) paperarc$helpers()[0]
                .invoke(this.getHandle(), ((CraftEntity) entity).getHandle());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to invoke Projectile#canHitEntity", e);
        }
    }

    @Unique
    public UUID getOwnerUniqueId() {
        return this.ownerUUID;
    }

    @Unique
    public boolean hasBeenShot() {
        return ApiState.get(this.getHandle(), PAPERARC$KEY_HAS_BEEN_SHOT, Boolean.FALSE);
    }

    @Unique
    public boolean hasLeftShooter() {
        return this.leftOwner;
    }

    @Unique
    public void hitEntity(org.bukkit.entity.Entity entity) {
        Preconditions.checkState(!this.getHandle().isRemoved(),
            "Cannot hit entity on a removed projectile");
        EntityHitResult hitResult =
            new EntityHitResult(((CraftEntity) entity).getHandle());
        try {
            paperarc$helpers()[1].invoke(this.getHandle(), hitResult);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to invoke Projectile#onHit", e);
        }
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
        this.leftOwner = hasLeftShooter;
    }

    /**
     * Resolves the protected vanilla helpers once:
     * [0] canHitEntity(Entity), [1] onHit(HitResult).
     */
    @Unique
    private static Method[] paperarc$helpers() throws NoSuchMethodException {
        Method[] helpers = PAPERARC$PROJ_HELPERS;
        if (helpers == null) {
            synchronized (CraftProjectileApiMixin.class) {
                if (PAPERARC$PROJ_HELPERS == null) {
                    Method canHit = Projectile.class.getDeclaredMethod("canHitEntity",
                        net.minecraft.world.entity.Entity.class);
                    canHit.setAccessible(true);
                    Method onHit = Projectile.class.getDeclaredMethod("onHit",
                        HitResult.class);
                    onHit.setAccessible(true);
                    PAPERARC$PROJ_HELPERS = new Method[]{canHit, onHit};
                }
                helpers = PAPERARC$PROJ_HELPERS;
            }
        }
        return helpers;
    }
}
