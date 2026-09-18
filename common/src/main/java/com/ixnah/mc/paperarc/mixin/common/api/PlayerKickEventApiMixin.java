package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.player.PlayerKickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code PlayerKickEvent} Component 版 reason / leaveMessage（A6/X-2 第六批）。
 * {@code getCause()} 需要踢出点传 Cause，不做假实现，见 docs/gaps.md。
 */
@Mixin(PlayerKickEvent.class)
public abstract class PlayerKickEventApiMixin {

    @Unique
    private PlayerKickEvent paperarc$self() {
        return (PlayerKickEvent) (Object) this;
    }

    @Unique
    public Component reason() {
        String legacy = this.paperarc$self().getReason();
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public void reason(Component reason) {
        this.paperarc$self().setReason(reason == null ? null
                : LegacyComponentSerializer.legacySection().serialize(reason));
    }

    @Unique
    public Component leaveMessage() {
        String legacy = this.paperarc$self().getLeaveMessage();
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public void leaveMessage(Component leaveMessage) {
        this.paperarc$self().setLeaveMessage(leaveMessage == null ? null
                : LegacyComponentSerializer.legacySection().serialize(leaveMessage));
    }
}
