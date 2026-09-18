package com.ixnah.mc.paperarc.bridge;

/**
 * Paper 给 sculk 感测器加的监听范围覆盖值（{@code SculkSensor#get/setListenerRange}）。
 *
 * <p>Paper 把 {@code rangeOverride} 放在 NMS {@code SculkSensorBlockEntity} 上，并让内部类
 * {@code VibrationUser#getListenerRadius()} 优先读它。这里落在
 * {@code SculkSensorBlockEntity$VibrationUser} 上（每个 BlockEntity 在构造器里建一个，
 * 一一对应），这样 {@code getListenerRadius()} 的两个实现（普通 / 校准）都能直接读到，
 * 不必从内部类回取外层实例。BlockEntity 侧的 NBT 存取经
 * {@code getVibrationUser()} 拿到同一个对象。
 *
 * <p>为什么不能挂在 Craft 侧：{@code CraftBlock#getState()} 每次返回**新快照**
 * （{@code javap -c} 核对 → {@code CraftBlockStates.getBlockState}），Craft 类上的
 * {@code @Unique} 字段活不过一次 {@code getState()}（checklist §1.10 an）。
 */
public interface SculkSensorRangeBridge {

    /** null = 未设置，按 vanilla 的监听半径。 */
    Integer paperarc$getRangeOverride();

    void paperarc$setRangeOverride(Integer range);
}
