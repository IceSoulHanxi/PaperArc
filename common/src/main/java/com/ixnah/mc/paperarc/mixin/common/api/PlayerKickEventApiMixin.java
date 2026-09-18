package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.player.PlayerKickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code PlayerKickEvent} Component 版 reason / leaveMessage（A6/X-2 第六批）。
 * <p>A7/Y-2 批 2 补 {@code getCause()}：Arclight 只有一处构造这个事件
 * （{@code ServerGamePacketListenerImpl#disconnect(String)}，签名是 spigot 老版本），
 * 于是由各踢出入口把 Cause 压进 {@link EventCauseState}，构造器里取。
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

    @Unique
    private PlayerKickEvent.Cause paperarc$cause;

    @Inject(method = "<init>(Lorg/bukkit/entity/Player;Ljava/lang/String;Ljava/lang/String;)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$captureCause(Player player, String reason, String leaveMessage, CallbackInfo ci) {
        this.paperarc$cause = EventCauseState.takeKickCause();
    }

    @Unique
    public PlayerKickEvent.Cause getCause() {
        PlayerKickEvent.Cause cause = this.paperarc$cause;
        return cause == null ? PlayerKickEvent.Cause.UNKNOWN : cause;
    }
}
