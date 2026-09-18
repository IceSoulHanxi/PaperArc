package com.ixnah.mc.paperarc.mixin.common.server;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 断线时关掉打开的界面：{@code InventoryCloseEvent.Reason.DISCONNECT}（Paper 同）。 */
@Mixin(PlayerList.class)
public abstract class PlayerListDisconnectCloseMixin {

    @Inject(method = "remove", at = @At("HEAD"))
    private void paperarc$disconnectClose(ServerPlayer player, CallbackInfo ci) {
        PaperarcEventCauses.pushInventoryClose(InventoryCloseEvent.Reason.DISCONNECT);
    }

    @Inject(method = "remove", at = @At("RETURN"))
    private void paperarc$disconnectCloseDone(ServerPlayer player, CallbackInfo ci) {
        PaperarcEventCauses.popInventoryClose();
    }
}
