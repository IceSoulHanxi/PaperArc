package dev.paperarc.mixin.common.player;

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent;
import com.mojang.authlib.GameProfile;
import dev.paperarc.event.PaperArcEvents;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * PlayerConnectionCloseEvent 触发点。
 * <p>
 * 对照 Paper：Connection#disconnect 内、packetListener.onDisconnect 之后，
 * 当 packetListener 为 ServerCommonPacketListenerImpl 且已拿到玩家
 * GameProfile 时发出。注入 disconnect(DisconnectionDetails) 的 RETURN
 * 近似同一时机。
 * <p>
 * v1 简化：未区分 Paper 补丁内的两处 onDisconnect 调用块（正常断开与
 * 登录阶段），统一在方法返回处判定一次；待真实代码核对后细化。
 */
@Mixin(Connection.class)
public abstract class ConnectionMixin {

    @Shadow
    private volatile PacketListener packetListener;

    @Shadow
    @Final
    private SocketAddress address;

    @Inject(
        method = "disconnect(Lnet/minecraft/network/DisconnectionDetails;)V",
        at = @At("RETURN")
    )
    private void paperarc$onConnectionClose(DisconnectionDetails details, CallbackInfo ci) {
        if (!(this.packetListener instanceof ServerCommonPacketListenerImpl common)) {
            return;
        }
        GameProfile profile = common.getOwner();
        if (profile == null) {
            return; // 登录完成前断开，无玩家身份
        }
        InetAddress inetAddress =
            this.address instanceof InetSocketAddress socketAddress ? socketAddress.getAddress() : null;
        PaperArcEvents.fire(new PlayerConnectionCloseEvent(profile.getId(), profile.getName(), inetAddress, false));
    }
}
