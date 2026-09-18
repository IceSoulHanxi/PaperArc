package com.ixnah.mc.paperarc.mixin.common.api;

import org.spigotmc.event.entity.EntityDismountEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** 同 {@link VehicleExitEventApiMixin}：paper 的二参构造器传 {@code true}，运行时只有二参构造器。 */
@Mixin(EntityDismountEvent.class)
public abstract class EntityDismountEventApiMixin {

    @Unique
    public boolean isCancellable() {
        return true;
    }
}
