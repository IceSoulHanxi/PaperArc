package com.ixnah.mc.paperarc.bridge;

/**
 * Duck interface exposing Paper's {@code BeaconBlockEntity.effectRange}
 * supplementary field (Custom-Potion-Mixes / beacon effect range API).
 * Paper 用 {@code -1} 表示"按信标等级算默认值"，没有 NMS 访问器，所以桥方法带
 * {@code paper$} 前缀。
 */
public interface BeaconBlockEntityBridge {

    double paper$getEffectRange();

    void paper$setEffectRange(double effectRange);
}
