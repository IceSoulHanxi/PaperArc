package com.ixnah.mc.paperarc.mixin.common.player;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 玩家自己按 E/ESC 关界面：{@code InventoryCloseEvent.Reason.PLAYER}（Paper 同）。 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class InventoryCloseReasonMixin {

    @Inject(method = "handleContainerClose", at = @At("HEAD"))
    private void paperarc$playerClose(ServerboundContainerClosePacket packet, CallbackInfo ci) {
        PaperarcEventCauses.pushInventoryClose(InventoryCloseEvent.Reason.PLAYER);
    }

    @Inject(method = "handleContainerClose", at = @At("RETURN"))
    private void paperarc$playerCloseDone(ServerboundContainerClosePacket packet, CallbackInfo ci) {
        PaperarcEventCauses.popInventoryClose();
    }
}
