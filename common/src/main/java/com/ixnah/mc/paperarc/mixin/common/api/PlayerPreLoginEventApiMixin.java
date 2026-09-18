package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.player.PlayerPreLoginEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** paper 的 {@code PlayerPreLoginEvent} Component 版 kickMessage / disallow（A6/X-2 第六批）。 */
@Mixin(PlayerPreLoginEvent.class)
public abstract class PlayerPreLoginEventApiMixin {

    @Unique
    @SuppressWarnings("deprecation")
    private PlayerPreLoginEvent paperarc$self() {
        return (PlayerPreLoginEvent) (Object) this;
    }

    @Unique
    @SuppressWarnings("deprecation")
    public Component kickMessage() {
        String legacy = this.paperarc$self().getKickMessage();
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    @SuppressWarnings("deprecation")
    public void kickMessage(Component kickMessage) {
        this.paperarc$self().setKickMessage(kickMessage == null ? null
                : LegacyComponentSerializer.legacySection().serialize(kickMessage));
    }

    @Unique
    @SuppressWarnings("deprecation")
    public void disallow(PlayerPreLoginEvent.Result result, Component message) {
        this.paperarc$self().disallow(result, message == null ? null
                : LegacyComponentSerializer.legacySection().serialize(message));
    }
}
