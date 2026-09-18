package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.server.ServerListPingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** paper 的 adventure 版 {@code motd()}（checklist §1.10 am，第 ④ 批）。 */
@Mixin(ServerListPingEvent.class)
public abstract class ServerListPingEventApiMixin {

    @Unique
    public Component motd() {
        String legacy = ((ServerListPingEvent) (Object) this).getMotd();
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public void motd(Component motd) {
        ((ServerListPingEvent) (Object) this).setMotd(
                motd == null ? null : LegacyComponentSerializer.legacySection().serialize(motd));
    }
}
