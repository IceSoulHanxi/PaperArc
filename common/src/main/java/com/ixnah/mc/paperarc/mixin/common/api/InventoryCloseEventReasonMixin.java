package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code InventoryCloseEvent#getReason()}（checklist E1）。
 * 原因由上游调用点压进 {@link PaperarcEventCauses}：
 * 插件的 {@code closeInventory()}/{@code closeInventory(Reason)} 在
 * {@code api.CraftHumanEntityCloseInventoryMixin}，玩家自己关界面与断线在
 * {@code player.InventoryCloseReasonMixin}。
 */
@Mixin(InventoryCloseEvent.class)
public abstract class InventoryCloseEventReasonMixin {

    @Unique
    private InventoryCloseEvent.Reason paperarc$reason = InventoryCloseEvent.Reason.UNKNOWN;

    @Inject(method = "<init>(Lorg/bukkit/inventory/InventoryView;)V", at = @At("RETURN"))
    private void paperarc$captureReason(InventoryView view, CallbackInfo ci) {
        this.paperarc$reason = PaperarcEventCauses.inventoryClose();
    }

    @Unique
    public InventoryCloseEvent.Reason getReason() {
        return this.paperarc$reason;
    }
}
