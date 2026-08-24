package com.ixnah.mc.paperarc.bridge;

/**
 * Cross-mixin hand-off of EntityLoadCrossbowEvent#shouldConsumeItem from the
 * CrossbowItem releaseUsing wrap into ProjectileWeaponItem's draw path.
 */
public final class CrossbowState {

    public static final ThreadLocal<Boolean> CONSUME_ITEM = ThreadLocal.withInitial(() -> Boolean.TRUE);

    private CrossbowState() {
    }
}
