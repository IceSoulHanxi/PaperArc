package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.ServerPlayerQuitReasonBridge;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code PlayerQuitEvent#getReason()}（{@code Add-API-for-quit-reason.patch}）。
 * 原因挂在 {@code ServerPlayer} 上（见 {@code player.ServerPlayerQuitReasonMixin}），
 * 由 {@code player.ConnectionQuitReasonMixin} 与
 * {@code player.ServerCommonPacketListenerKickMixin} 在网络层写入；
 * 这里在事件构造器里读回来。
 */
@Mixin(PlayerQuitEvent.class)
public abstract class PlayerQuitEventReasonMixin {

    @Unique
    private PlayerQuitEvent.QuitReason paperarc$reason = PlayerQuitEvent.QuitReason.DISCONNECTED;

    @Inject(method = "<init>(Lorg/bukkit/entity/Player;Ljava/lang/String;)V", at = @At("RETURN"))
    private void paperarc$captureReason(Player who, String quitMessage, CallbackInfo ci) {
        if (who instanceof CraftPlayer craft) {
            this.paperarc$reason = ((ServerPlayerQuitReasonBridge) craft.getHandle()).paperarc$getQuitReason();
        }
    }

    @Unique
    public PlayerQuitEvent.QuitReason getReason() {
        return this.paperarc$reason;
    }
}
