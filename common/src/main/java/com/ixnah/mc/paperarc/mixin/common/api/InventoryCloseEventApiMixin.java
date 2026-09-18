package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paper 的 {@code InventoryCloseEvent#getReason}（checklist E1）。
 *
 * <p>Arclight 只有一处构造这个事件（{@code CraftEventFactory#handleInventoryCloseEvent}），
 * 改不了形参，于是在构造器里取 {@link EventCauseState} 里由各关闭入口压进来的原因。
 * 压值的入口见 {@code InventoryCloseReasonMixin} 与 {@code CraftHumanEntityApiMixin}。
 */
@Mixin(InventoryCloseEvent.class)
public abstract class InventoryCloseEventApiMixin {

    @Unique
    private InventoryCloseEvent.Reason paperarc$reason;

    @Inject(method = "<init>(Lorg/bukkit/inventory/InventoryView;)V", at = @At("RETURN"), remap = false)
    private void paperarc$captureReason(InventoryView transaction, CallbackInfo ci) {
        this.paperarc$reason = EventCauseState.takeInventoryCloseReason();
    }

    @Unique
    public InventoryCloseEvent.Reason getReason() {
        InventoryCloseEvent.Reason reason = this.paperarc$reason;
        return reason == null ? InventoryCloseEvent.Reason.UNKNOWN : reason;
    }
}
