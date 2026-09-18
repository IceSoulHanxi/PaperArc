package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcPortals;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code EntityPortalEnterEvent}：{@code getPortalType()} 与 {@code Cancellable}
 * （{@code Improve-PortalEvents.patch}）。
 *
 * <p>类型直接看事件自带的那个 Location 上的方块，不需要触发点配合。
 * 取消由 {@code entity.PortalBlockEnterCancelMixin} 在两个传送门方块的
 * {@code entityInside} 里消费（Arclight 在那里 fire 事件，我们的注入器排在它之后）。
 */
@Mixin(EntityPortalEnterEvent.class)
public abstract class EntityPortalEnterEventApiMixin implements Cancellable {

    @Unique
    private boolean paperarc$cancelled;

    @Unique
    private PortalType paperarc$portalType = PortalType.CUSTOM;

    @Inject(method = "<init>(Lorg/bukkit/entity/Entity;Lorg/bukkit/Location;)V", at = @At("RETURN"))
    private void paperarc$capturePortalType(Entity entity, Location location, CallbackInfo ci) {
        this.paperarc$portalType = PaperarcPortals.fromBlock(location);
        com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses.rememberPortalEnterEvent(
                (EntityPortalEnterEvent) (Object) this);
    }

    @Unique
    public PortalType getPortalType() {
        return this.paperarc$portalType;
    }

    @Unique
    public boolean isCancelled() {
        return this.paperarc$cancelled;
    }

    @Unique
    public void setCancelled(boolean cancel) {
        this.paperarc$cancelled = cancel;
    }
}
