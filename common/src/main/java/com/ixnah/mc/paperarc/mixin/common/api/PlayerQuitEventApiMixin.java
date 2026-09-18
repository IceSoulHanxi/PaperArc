package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** paper 的 adventure 版 {@code quitMessage()}（checklist §1.10 am，第 ④ 批）。 */
@Mixin(PlayerQuitEvent.class)
public abstract class PlayerQuitEventApiMixin {

    @Unique
    public Component quitMessage() {
        String legacy = ((PlayerQuitEvent) (Object) this).getQuitMessage();
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public void quitMessage(Component quitMessage) {
        ((PlayerQuitEvent) (Object) this).setQuitMessage(
                quitMessage == null ? null : LegacyComponentSerializer.legacySection().serialize(quitMessage));
    }
}
