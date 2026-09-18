package com.ixnah.mc.paperarc.bridge;

/**
 * Paper 在 {@code LivingEntity} 上补充的几个纯 API 状态（无 NMS 访问器），
 * 由 {@code mojmap/entity/LivingEntityFieldsMixin} 注入。
 * 注意与已有的 {@code mixin.common.LivingEntityBridge}（暴露 setJumping）区分。
 */
public interface LivingEntityFieldsBridge {

    float paper$getUpwardsMovement();

    void paper$setUpwardsMovement(float upwardsMovement);

    net.kyori.adventure.util.TriState paper$getFrictionState();

    void paper$setFrictionState(net.kyori.adventure.util.TriState frictionState);

    int paper$getShieldBlockingDelay();

    void paper$setShieldBlockingDelay(int shieldBlockingDelay);
}
