package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import org.bukkit.craftbukkit.v.entity.CraftHumanEntity;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code HumanEntity#closeInventory(InventoryCloseEvent.Reason)}。
 *
 * <p>B7/Y-2 批 2 之前这个实现体把 reason 直接丢掉（checklist E1 的"未透传"）：
 * spigot 的 NMS 没有带 Reason 的 {@code closeContainer}，Arclight 也没有。
 * 现在改成把 reason 压进 {@link PaperarcEventCauses} 再关 —— 事件由 CraftBukkit 在
 * {@code closeContainer()} 内部构造，构造器（也是我们的 mixin，见
 * {@code InventoryCloseEventReasonMixin}）读回来。
 *
 * <p>无参的 {@code closeInventory()} 按 Paper 记 {@code PLUGIN}。
 */
@Mixin(CraftHumanEntity.class)
public abstract class CraftHumanEntityCloseInventoryMixin {

    @Unique
    public void closeInventory(InventoryCloseEvent.Reason reason) {
        PaperarcEventCauses.pushInventoryClose(reason == null ? InventoryCloseEvent.Reason.UNKNOWN : reason);
        try {
            ((org.bukkit.craftbukkit.v.entity.CraftPlayer) (Object) this).getHandle().closeContainer();
        } finally {
            PaperarcEventCauses.popInventoryClose();
        }
    }

    @Inject(method = "closeInventory()V", at = @At("HEAD"), remap = false)
    private void paperarc$pluginClose(CallbackInfo ci) {
        PaperarcEventCauses.pushInventoryClose(InventoryCloseEvent.Reason.PLUGIN);
    }

    @Inject(method = "closeInventory()V", at = @At("RETURN"), remap = false)
    private void paperarc$pluginCloseDone(CallbackInfo ci) {
        PaperarcEventCauses.popInventoryClose();
    }
}
