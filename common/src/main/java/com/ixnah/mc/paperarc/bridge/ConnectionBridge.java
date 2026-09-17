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
}
