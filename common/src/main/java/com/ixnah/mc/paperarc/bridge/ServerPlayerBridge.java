package com.ixnah.mc.paperarc.bridge;

/**
 * Duck interface for the supplementary fields injected onto NMS
 * {@code ServerPlayer}. Paper patches {@code loginTime} onto ServerPlayer
 * (Player-Connection-Close-Event / OfflinePlayer-API); vanilla has no such
 * field, so it is injected by {@code ServerPlayerFieldsMixin}.
 */
public interface ServerPlayerBridge {

    long paper$loginTime();
}
