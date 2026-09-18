package com.ixnah.mc.paperarc.mixin.common.player;

import com.ixnah.mc.paperarc.bridge.ServerPlayerQuitReasonBridge;
import io.netty.handler.timeout.TimeoutException;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paper {@code Add-API-for-quit-reason.patch} 的 {@code Connection} 一侧：
 * 网络层异常时把退出原因标成 {@code TIMED_OUT} / {@code ERRONEOUS_STATE}。
 * 正常断开走字段默认值 {@code DISCONNECTED}，被踢由
 * {@code ServerCommonPacketListenerKickMixin} 改成 {@code KICKED}。
 */
@Mixin(Connection.class)
public abstract class ConnectionQuitReasonMixin {

    @Shadow
    private volatile PacketListener packetListener;

    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    private void paperarc$markQuitReason(io.netty.channel.ChannelHandlerContext context, Throwable throwable,
                                         CallbackInfo ci) {
        if (this.packetListener instanceof ServerGamePacketListenerImpl listener) {
            ((ServerPlayerQuitReasonBridge) listener.player).paperarc$setQuitReason(
                    throwable instanceof TimeoutException
                            ? PlayerQuitEvent.QuitReason.TIMED_OUT
                            : PlayerQuitEvent.QuitReason.ERRONEOUS_STATE);
        }
    }
}
