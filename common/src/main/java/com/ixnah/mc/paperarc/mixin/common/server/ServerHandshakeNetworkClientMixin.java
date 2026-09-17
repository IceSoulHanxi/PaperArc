package com.ixnah.mc.paperarc.mixin.common.server;

import com.ixnah.mc.paperarc.bridge.ConnectionBridge;
import java.net.InetSocketAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Records the handshake's protocol version and virtual host onto the
 * {@link Connection} so {@code com.destroystokyo.paper.network.NetworkClient}
 * ({@code Player.getProtocolVersion()} / {@code getVirtualHost()}) can answer.
 * Vanilla throws the {@code ClientIntentionPacket} away after dispatching, and
 * Arclight only keeps a {@code "host:port"} string for the Spigot
 * {@code PlayerHandshakeEvent} plumbing, so the values have to be captured here.
 *
 * <p>{@code ClientIntentionPacket} 在 1.21.1 是 record，访问器是
 * {@code protocolVersion()}/{@code hostName()}/{@code port()}（1.20.1 是
 * {@code getProtocolVersion()} 那一套）。
 *
 * <p>Arclight {@code @Overwrite}s {@code handleIntention}; an {@code @At("HEAD")}
 * inject is unaffected by that (it only prepends to whatever body ends up in the
 * method) and matches Paper's capture point, which is likewise the first
 * statement — i.e. before Spigot's BungeeCord split rewrites the host name.
 */
@Mixin(ServerHandshakePacketListenerImpl.class)
public abstract class ServerHandshakeNetworkClientMixin {

    @Shadow
    @Final
    private Connection connection;

    @Inject(method = "handleIntention", at = @At("HEAD"))
    private void paperarc$captureNetworkClient(ClientIntentionPacket packet, CallbackInfo ci) {
        ConnectionBridge bridge = (ConnectionBridge) this.connection;
        bridge.paper$setProtocolVersion(packet.protocolVersion());
        bridge.paper$setVirtualHost(
                InetSocketAddress.createUnresolved(packet.hostName(), packet.port()));
    }
}
