package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.player.PlayerJoinEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** paper 的 {@code PlayerJoinEvent#joinMessage}（A6/X-2 第六批，包装事件已有的 String）。 */
@Mixin(PlayerJoinEvent.class)
public abstract class PlayerJoinEventApiMixin {

    @Unique
    public Component joinMessage() {
        String legacy = ((PlayerJoinEvent) (Object) this).getJoinMessage();
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public void joinMessage(Component joinMessage) {
        ((PlayerJoinEvent) (Object) this).setJoinMessage(joinMessage == null ? null
                : LegacyComponentSerializer.legacySection().serialize(joinMessage));
    }
}
