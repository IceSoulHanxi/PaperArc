package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** paper 的 adventure 版 {@code message()}（checklist §1.10 am，第 ④ 批）。 */
@Mixin(BroadcastMessageEvent.class)
public abstract class BroadcastMessageEventApiMixin {

    @Unique
    public Component message() {
        String legacy = ((BroadcastMessageEvent) (Object) this).getMessage();
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public void message(Component message) {
        ((BroadcastMessageEvent) (Object) this).setMessage(
                message == null ? null : LegacyComponentSerializer.legacySection().serialize(message));
    }
}
