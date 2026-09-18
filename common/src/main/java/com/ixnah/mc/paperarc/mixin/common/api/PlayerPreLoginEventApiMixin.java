package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** paper 的 adventure 版 {@code kickMessage()} 与 {@code disallow(Result, Component)}。 */
@Mixin(PlayerPreLoginEvent.class)
public abstract class PlayerPreLoginEventApiMixin {

    @Unique
    private PlayerPreLoginEvent paperarc$self() {
        return (PlayerPreLoginEvent) (Object) this;
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
    public void disallow(PlayerPreLoginEvent.Result result, Component message) {
        this.paperarc$self().disallow(result,
                message == null ? null : LegacyComponentSerializer.legacySection().serialize(message));
    }
}
