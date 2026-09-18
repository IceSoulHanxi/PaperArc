package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcPortals;
import org.bukkit.PortalType;
import org.bukkit.event.entity.EntityPortalEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code EntityPortalEvent#getPortalType()}（{@code Improve-PortalEvents.patch}）。
 * 事件发生时实体正在传送门里，按它的 {@code portalProcess} 判定，不需要触发点配合。
 */
@Mixin(EntityPortalEvent.class)
public abstract class EntityPortalEventApiMixin {

    @Unique
    public PortalType getPortalType() {
        return PaperarcPortals.fromEntity(((EntityPortalEvent) (Object) this).getEntity());
    }
}
