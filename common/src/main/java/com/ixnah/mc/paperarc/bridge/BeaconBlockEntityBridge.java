package com.ixnah.mc.paperarc.bridge;

/**
 * Paper 给 NMS {@code BeaconBlockEntity} 加的 {@code effectRange}（A5-3）。
 * vanilla 没有这个字段，由 {@code BeaconBlockEntityFieldsMixin} 注入。
 */
public interface BeaconBlockEntityBridge {

    double paper$getEffectRange();

    void paper$setEffectRange(double range);
}
