package com.ixnah.mc.paperarc.mixin.common.player;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 玩家死亡时关闭容器界面的 {@code InventoryCloseEvent#getReason} = {@code DEATH}。
 * 锚点在 Arclight {@code @Overwrite} 后的 {@code ServerPlayer#die} 体里
 * （{@code if (this.containerMenu != this.inventoryMenu) this.closeContainer();}，只有一处）。
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDeathCloseReasonMixin {

    @WrapOperation(
            method = "die",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;closeContainer()V"))
    private void paperarc$deathCloseReason(ServerPlayer self, Operation<Void> original) {
        EventCauseState.setInventoryCloseReason(InventoryCloseEvent.Reason.DEATH);
        try {
            original.call(self);
        } finally {
            EventCauseState.clearInventoryCloseReason();
        }
    }
}
