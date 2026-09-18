package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.player.PlayerKickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 adventure 版 {@code leaveMessage()} / {@code reason()}
 * （checklist §1.10 am，第 ④ 批）。
 *
 * <p>没补 {@code getCause()}：运行时的 PlayerKickEvent 不带 cause，Arclight 触发时
 * 也没有可映射的来源，返回任何一个枚举值都是编造。见 docs/gaps.md。</p>
 */
@Mixin(PlayerKickEvent.class)
public abstract class PlayerKickEventApiMixin {

    @Unique
    private PlayerKickEvent paperarc$self() {
        return (PlayerKickEvent) (Object) this;
    }

    @Unique
    public Component leaveMessage() {
        String legacy = this.paperarc$self().getLeaveMessage();
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public void leaveMessage(Component leaveMessage) {
        this.paperarc$self().setLeaveMessage(
                leaveMessage == null ? null : LegacyComponentSerializer.legacySection().serialize(leaveMessage));
    }

    @Unique
    public Component reason() {
        String legacy = this.paperarc$self().getReason();
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public void reason(Component kickReason) {
        this.paperarc$self().setReason(
                kickReason == null ? null : LegacyComponentSerializer.legacySection().serialize(kickReason));
    }
}
