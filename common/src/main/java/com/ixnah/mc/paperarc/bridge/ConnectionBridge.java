package com.ixnah.mc.paperarc.bridge;

import java.net.InetSocketAddress;

/**
 * Duck interface exposing Paper's {@code Connection.protocolVersion} /
 * {@code Connection.virtualHost} supplementary fields (Paper patch
 * {@code Add-more-fields-to-the-NetworkClient}）to the api mixins.
 * Paper adds the fields with no accessor methods, so the bridge methods carry
 * the {@code paper$} prefix.
 */
public interface ConnectionBridge {

    int paper$getProtocolVersion();

    void paper$setProtocolVersion(int protocolVersion);

    InetSocketAddress paper$getVirtualHost();

    void paper$setVirtualHost(InetSocketAddress virtualHost);

    /**
     * 代理改写之前的真实对端地址（Paper 的 {@code AsyncPlayerPreLoginEvent#getRawAddress}）。
     * Spigot 的 BungeeCord 支持会把 {@code Connection.address} 换成转发来的玩家地址，
     * 但 netty channel 上的对端地址不会变。
     */
    java.net.InetAddress paper$getRawAddress();
}
