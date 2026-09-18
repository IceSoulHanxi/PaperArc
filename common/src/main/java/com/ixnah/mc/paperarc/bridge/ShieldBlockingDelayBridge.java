package com.ixnah.mc.paperarc.bridge;

/**
 * Paper 的 {@code LivingEntity.shieldBlockingDelay}（{@code LivingEntity#get/setShieldBlockingDelay}）。
 *
 * <p>Paper 把它放在 NMS {@code LivingEntity} 上，而且 vanilla 的 {@code isBlocking()} 里那个
 * 硬编码的 {@code 5} 被换成 {@code getShieldBlockingDelay()} —— 状态必须在 NMS 侧，
 * 否则设了也不影响真实的举盾判定（原实现放在 Craft 侧注入字段，只有读写、不生效）。
 */
public interface ShieldBlockingDelayBridge {

    int paperarc$getShieldBlockingDelay();

    void paperarc$setShieldBlockingDelay(int delay);
}
