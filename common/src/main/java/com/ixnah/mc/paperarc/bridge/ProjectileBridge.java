package com.ixnah.mc.paperarc.bridge;

/**
 * Paper 的 {@code Projectile.hasBeenShot}（Projectile-Hit-API 之外的补充字段，
 * Paper 以裸字段形式加，没有 NMS 访问器），由 {@code mojmap/entity/ProjectileFieldsMixin} 注入。
 */
public interface ProjectileBridge {

    boolean paper$hasBeenShot();

    void paper$setHasBeenShot(boolean hasBeenShot);
}
