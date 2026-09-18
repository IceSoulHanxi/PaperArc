package com.ixnah.mc.paperarc.bridge;

/**
 * Duck interface for Paper 的 {@code SculkSensorBlockEntity.rangeOverride}
 * （Configurable-sculk-sensor-listener-range.patch）。
 *
 * <p>同一个接口同时挂在 {@code SculkSensorBlockEntity} 与它的
 * {@code VibrationUser} 内部类上：前者负责 NBT 持久化与 API 读写，后者负责
 * {@code getListenerRadius()} 真的返回覆盖值。{@code null} = 未设置。</p>
 *
 * <p>为什么不放在 {@code CraftSculkSensor} 上：方块状态是快照，
 * {@code Block#getState()} 每次 new 一个新的 Craft 包装对象
 * （javap：{@code CraftBlockStates.getBlockState} 方法体逐次 new），
 * 挂在包装对象上的状态取一次就丢（checklist §1.10 an）。</p>
 */
public interface SculkSensorRangeBridge {

    Integer paperarc$getRangeOverride();

    void paperarc$setRangeOverride(Integer rangeOverride);
}
