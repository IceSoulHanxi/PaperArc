package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code PlayerQuitEvent#quitMessage}（A6/X-2 第六批）。
 * <p>A7/Y-2 批 2 补 {@code getReason()}：由退出路径把 QuitReason 压进
 * {@link EventCauseState}（走 {@code disconnect} 的是 KICKED、keep-alive 超时是 TIMED_OUT，
 * 其余是 Paper 的默认值 DISCONNECTED），构造器里取。
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

    @Unique
    private PlayerQuitEvent.QuitReason paperarc$reason;

    @Inject(method = "<init>(Lorg/bukkit/entity/Player;Ljava/lang/String;)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$captureReason(Player player, String quitMessage, CallbackInfo ci) {
        this.paperarc$reason = EventCauseState.takeQuitReason();
    }

    @Unique
    public PlayerQuitEvent.QuitReason getReason() {
        PlayerQuitEvent.QuitReason reason = this.paperarc$reason;
        return reason == null ? PlayerQuitEvent.QuitReason.DISCONNECTED : reason;
    }
}
