package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code AsyncPlayerPreLoginEvent} Component 版 kickMessage / 两个 disallow
 * （A6/X-2 第六批）。
 *
 * <p>{@code getHostname()}/{@code getRawAddress()}/{@code get-setPlayerProfile()} 要靠
 * 登录握手那一侧把数据传进事件，不做假实现，见 docs/gaps.md。
 */
@Mixin(AsyncPlayerPreLoginEvent.class)
public abstract class AsyncPlayerPreLoginEventApiMixin {

    @Unique
    private AsyncPlayerPreLoginEvent paperarc$self() {
        return (AsyncPlayerPreLoginEvent) (Object) this;
    }

    @Unique
    public Component kickMessage() {
        String legacy = this.paperarc$self().getKickMessage();
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public void kickMessage(Component kickMessage) {
        this.paperarc$self().setKickMessage(kickMessage == null ? null
                : LegacyComponentSerializer.legacySection().serialize(kickMessage));
    }

    @Unique
    public void disallow(AsyncPlayerPreLoginEvent.Result result, Component message) {
        this.paperarc$self().disallow(result, message == null ? null
                : LegacyComponentSerializer.legacySection().serialize(message));
    }

    @Unique
    @SuppressWarnings("deprecation")
    public void disallow(PlayerPreLoginEvent.Result result, Component message) {
        this.paperarc$self().disallow(result, message == null ? null
                : LegacyComponentSerializer.legacySection().serialize(message));
    }
}
