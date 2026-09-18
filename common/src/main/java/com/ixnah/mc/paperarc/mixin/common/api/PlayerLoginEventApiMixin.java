package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.player.PlayerLoginEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** paper 的 {@code PlayerLoginEvent} Component 版 kickMessage / disallow（A6/X-2 第六批）。 */
@Mixin(PlayerLoginEvent.class)
public abstract class PlayerLoginEventApiMixin {

    @Unique
    private PlayerLoginEvent paperarc$self() {
        return (PlayerLoginEvent) (Object) this;
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
    public void disallow(PlayerLoginEvent.Result result, Component message) {
        this.paperarc$self().disallow(result, message == null ? null
                : LegacyComponentSerializer.legacySection().serialize(message));
    }
}
