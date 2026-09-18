package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 adventure 版 {@code kickMessage()} 与两个 {@code disallow(..., Component)}
 * （checklist §1.10 am，第 ④ 批）。
 *
 * <p>没补 {@code getPlayerProfile()/setPlayerProfile()}、{@code getRawAddress()}、
 * {@code getHostname()}：运行时事件里没有对应字段，Arclight 触发点也不带这些信息，
 * 见 docs/gaps.md。</p>
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
    public void kickMessage(Component message) {
        this.paperarc$self().setKickMessage(
                message == null ? null : LegacyComponentSerializer.legacySection().serialize(message));
    }

    @Unique
    public void disallow(AsyncPlayerPreLoginEvent.Result result, Component message) {
        this.paperarc$self().disallow(result,
                message == null ? null : LegacyComponentSerializer.legacySection().serialize(message));
    }

    @Unique
    public void disallow(PlayerPreLoginEvent.Result result, Component message) {
        this.paperarc$self().disallow(result,
                message == null ? null : LegacyComponentSerializer.legacySection().serialize(message));
    }
}
