package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code PlayerQuitEvent#quitMessage}（A6/X-2 第六批）。
 * {@code getReason()} 需要退出处理那一侧传 QuitReason，不做假实现，见 docs/gaps.md。
 */
@Mixin(PlayerQuitEvent.class)
public abstract class PlayerQuitEventApiMixin {

    @Unique
    public Component quitMessage() {
        String legacy = ((PlayerQuitEvent) (Object) this).getQuitMessage();
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public void quitMessage(Component quitMessage) {
        ((PlayerQuitEvent) (Object) this).setQuitMessage(quitMessage == null ? null
                : LegacyComponentSerializer.legacySection().serialize(quitMessage));
    }
}
