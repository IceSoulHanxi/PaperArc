package com.ixnah.mc.paperarc.mixin.common.network;

import com.ixnah.mc.paperarc.bridge.ConnectionBridge;
import java.net.InetSocketAddress;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code Connection.protocolVersion} / {@code Connection.virtualHost}
 * supplementary fields, which back
 * {@code com.destroystokyo.paper.network.NetworkClient}. Field names match Paper
 * exactly (no {@code paperarc$} prefix) so reflection on the NMS class is
 * ABI-compatible with Paper; they are filled by
 * {@code mixin.common.server.ServerHandshakeNetworkClientMixin}.
 */
@Mixin(Connection.class)
public abstract class ConnectionFieldsMixin implements ConnectionBridge {

    @Unique
    public int protocolVersion; // Paper

    @Unique
    public InetSocketAddress virtualHost; // Paper

    @Override
    public int paper$getProtocolVersion() {
        return this.protocolVersion;
    }

    @Override
    public void paper$setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    @Override
    public InetSocketAddress paper$getVirtualHost() {
        return this.virtualHost;
    }

    @Override
    public void paper$setVirtualHost(InetSocketAddress virtualHost) {
        this.virtualHost = virtualHost;
    }
}
