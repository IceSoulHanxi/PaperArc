package com.ixnah.mc.paperarc.mixin.common.player;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 玩家下线时关闭容器界面的 {@code InventoryCloseEvent#getReason} = {@code DISCONNECT}。
 *
 * <p>Arclight 在 {@code PlayerList#remove} 里调的是 Bukkit 侧的
 * {@code CraftHumanEntity#closeInventory()}（那条只在"还没人给出原因"时才补 PLUGIN），
 * 所以在 remove 的 HEAD 先把 DISCONNECT 压上。
 */
@Mixin(PlayerList.class)
public abstract class PlayerListRemoveReasonMixin {

    @Inject(method = "remove", at = @At("HEAD"))
    private void paperarc$disconnectCloseReason(ServerPlayer player, CallbackInfo ci) {
        EventCauseState.setInventoryCloseReason(InventoryCloseEvent.Reason.DISCONNECT);
    }

    @Inject(method = "remove", at = @At("RETURN"))
    private void paperarc$clearDisconnectCloseReason(ServerPlayer player, CallbackInfo ci) {
        EventCauseState.clearInventoryCloseReason();
    }
}
