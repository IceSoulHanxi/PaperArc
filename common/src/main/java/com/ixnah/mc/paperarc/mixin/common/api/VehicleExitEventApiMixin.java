package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.event.vehicle.VehicleExitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code VehicleExitEvent#isCancellable()}。
 *
 * <p>paper 把它做成构造器形参存在 {@code isCancellable} 字段里；运行时只有不带该形参的
 * 二参构造器，而 paper 的二参构造器传的就是 {@code true}
 * （`javap -c` 核对 paper-api：{@code this(vehicle, exited, true)}）。
 * 所以在 Arclight 上恒 {@code true} 就是正确取值，不是占位。
 */
@Mixin(VehicleExitEvent.class)
public abstract class VehicleExitEventApiMixin {

    @Unique
    public boolean isCancellable() {
        return true;
    }
}
