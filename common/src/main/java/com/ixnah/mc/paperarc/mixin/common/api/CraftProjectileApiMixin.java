package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import com.ixnah.mc.paperarc.bridge.craft.CraftEntityBridge;
import org.bukkit.craftbukkit.v.entity.AbstractProjectile;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

/**
 * Port of Paper's More-Projectile-API additions on {@link AbstractProjectile}.
 *
 * <p>宿主是 {@code AbstractProjectile} 而不是 {@code CraftProjectile}：CB 里
 * {@code CraftArrow}/{@code CraftFireball}/{@code CraftLlamaSpit}/{@code CraftShulkerBullet}
 * 直接继承 {@code AbstractProjectile}，挂在 {@code CraftProjectile} 上这四个类走到就是
 * {@code AbstractMethodError}（A4-1 r 实测 32 条）。
 *
 * <p>Mappings to this codebase's NMS (mojmap 1.21.1 {@code Projectile}):
 * <ul>
 *   <li>{@code getOwnerUniqueId()} → private field {@code ownerUUID} (@Shadow).</li>
 *   <li>{@code hasLeftShooter()} / {@code setHasLeftShooter(boolean)} → private
 *       field {@code leftOwner} (@Shadow).</li>
 *   <li>{@code hasBeenShot()} / {@code setHasBeenShot(boolean)} → Paper keeps
 *       this flag in the NMS-patched {@code Projectile.hasBeenShot} field; the
 *       vanilla 1.20.1 NMS has the equivalent private field f_150164_, widened
 *       via AT (mojmap name {@code hasBeenShot}) and read/written directly.</li>
 *   <li>{@code canHitEntity(Entity)} → protected NMS
 *       {@code Projectile#canHitEntity(Entity)} via cached reflection.</li>
 *   <li>{@code hitEntity(...)} → protected NMS {@code Projectile#onHit(HitResult)}
 *       with an {@link EntityHitResult} targeting the hit entity, via cached
 *       reflection; guarded by the same removed-check Paper uses.</li>
 * </ul>
 */
@Mixin(AbstractProjectile.class)
public abstract class CraftProjectileApiMixin {

    /**
     * {@code AbstractProjectile} 自己没有收窄的 {@code getHandle()}（只有
     * {@code CraftProjectile} 有），所以走 {@code CraftEntity} 上的 duck 接口取句柄。
     */
    // 名字不能叫 getHandle：CraftEntity 上已有同名方法，Mixin 按方法名判冲突会
    // 直接丢弃整个 @Unique 方法（docs/mixin-conventions.md）。
    @Unique
    private Projectile paperarc$handle() {
        return (Projectile) ((CraftEntityBridge) (Object) this).paperarc$getHandle();
    }

    @Unique
    public boolean canHitEntity(org.bukkit.entity.Entity entity) {
        return this.paperarc$handle().canHitEntity(((CraftEntity) entity).getHandle());
    }

    @Unique
    public UUID getOwnerUniqueId() {
        return this.paperarc$handle().ownerUUID;
    }

    @Unique
    public boolean hasBeenShot() {
        return this.paperarc$handle().hasBeenShot;
    }

    @Unique
    public boolean hasLeftShooter() {
        return this.paperarc$handle().leftOwner;
    }

    @Unique
    public void hitEntity(org.bukkit.entity.Entity entity) {
        Preconditions.checkState(!this.paperarc$handle().isRemoved(),
            "Cannot hit entity on a removed projectile");
        this.paperarc$handle()
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
        this.paperarc$handle().hasBeenShot = hasBeenShot;
    }

    @Unique
    public void setHasLeftShooter(boolean hasLeftShooter) {
        this.paperarc$handle().leftOwner = hasLeftShooter;
    }
}
