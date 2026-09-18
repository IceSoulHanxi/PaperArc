package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.event.entity.EntityDismountEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * 同 {@link VehicleExitEventApiMixin}：paper 的二参构造器传 {@code true}，运行时只有二参构造器。
 *
 * <p>与 1.20.1 的差别：这个事件 1.21.1 已经从 {@code org.spigotmc.event.entity} 挪到了
 * {@code org.bukkit.event.entity}（`javap` 核对运行时只有后者）。
 */
@Mixin(EntityDismountEvent.class)
public abstract class EntityDismountEventApiMixin {

    @Unique
    public boolean isCancellable() {
        return true;
    }
}
