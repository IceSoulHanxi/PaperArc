package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import org.bukkit.craftbukkit.v.entity.AbstractProjectile;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.util.Vector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

/**
 * {@code org.bukkit.entity.Projectile} 的**第二条实现链**。
 *
 * <p>CraftBukkit 里 Projectile 有两族实现：{@code CraftProjectile}（大多数弹射物）与
 * {@code AbstractProjectile}（CraftAbstractArrow / CraftFireball / CraftLlamaSpit /
 * CraftShulkerBullet）。只给前者加实现体的话，箭、火球等一调 {@code hitEntity()}
 * 之类就是 {@code AbstractMethodError}（B2-4 的 PARTIAL_IMPL 门禁抓到）。
 * 实现体与 {@link CraftProjectileApiMixin} 逐字一致，只是 handle 要自己强转。</p>
 */
@Mixin(AbstractProjectile.class)
public abstract class AbstractProjectileApiMixin {

    @Unique
    private Projectile paperarc$nms() {
        return (Projectile) ((CraftEntity) (Object) this).getHandle();
    }

    @Unique
    public boolean canHitEntity(org.bukkit.entity.Entity entity) {
        return this.paperarc$nms().canHitEntity(((CraftEntity) entity).getHandle());
    }

    @Unique
    public UUID getOwnerUniqueId() {
        return this.paperarc$nms().ownerUUID;
    }

    @Unique
    public boolean hasBeenShot() {
        return ((com.ixnah.mc.paperarc.bridge.ProjectileBridge) this.paperarc$nms()).paper$hasBeenShot();
    }

    @Unique
    public boolean hasLeftShooter() {
        return this.paperarc$nms().leftOwner;
    }

    @Unique
    public void hitEntity(org.bukkit.entity.Entity entity) {
        Projectile handle = this.paperarc$nms();
        Preconditions.checkState(!handle.isRemoved(), "Cannot hit entity on a removed projectile");
        handle.onHit(new EntityHitResult(((CraftEntity) entity).getHandle()));
    }

    @Unique
    public void hitEntity(org.bukkit.entity.Entity entity, Vector vector) {
        // 同 CraftProjectileApiMixin：1.21.1 的 onHit 只有单参版本，方向提示无处可放
        this.hitEntity(entity);
    }

    @Unique
    public void setHasBeenShot(boolean hasBeenShot) {
        ((com.ixnah.mc.paperarc.bridge.ProjectileBridge) this.paperarc$nms()).paper$setHasBeenShot(hasBeenShot);
    }

    @Unique
    public void setHasLeftShooter(boolean hasLeftShooter) {
        this.paperarc$nms().leftOwner = hasLeftShooter;
    }
}
