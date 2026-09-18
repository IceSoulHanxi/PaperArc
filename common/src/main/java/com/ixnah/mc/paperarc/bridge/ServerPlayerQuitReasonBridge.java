package com.ixnah.mc.paperarc.bridge;

import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Paper 在 {@code ServerPlayer} 上加的 {@code quitReason} 字段（{@code Add-API-for-quit-reason.patch}）。
 * 断开原因在网络层就定了，事件却在 {@code PlayerList#remove} 才发，只能挂在玩家身上传。
 */
public interface ServerPlayerQuitReasonBridge {

    PlayerQuitEvent.QuitReason paperarc$getQuitReason();

    void paperarc$setQuitReason(PlayerQuitEvent.QuitReason reason);
}
