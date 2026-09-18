package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Paper 的 {@code PlayerGameModeChangeEvent#getCause/cancelMessage}。
 *
 * <p>{@code cause} 由各入口压进 {@link EventCauseState}（插件 {@code setGameMode} →
 * PLUGIN、{@code /gamemode} → COMMAND、玩家进服套默认游戏模式 → DEFAULT_GAMEMODE，
 * 其余 UNKNOWN，与 Paper 的取值一致）。
 *
 * <p>{@code cancelMessage} 的消费方是 {@code /gamemode} 命令：事件被取消时把它发给命令发送者
 * （见 {@code GameModeCommandCauseMixin}）。
 */
@Mixin(PlayerGameModeChangeEvent.class)
public abstract class PlayerGameModeChangeEventApiMixin {

    @Unique
    private PlayerGameModeChangeEvent.Cause paperarc$cause;

    @Unique
    private Component paperarc$cancelMessage;

    @Inject(method = "<init>(Lorg/bukkit/entity/Player;Lorg/bukkit/GameMode;)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$captureCause(Player player, GameMode newGameMode, CallbackInfo ci) {
        this.paperarc$cause = EventCauseState.takeGameModeCause();
        EventCauseState.setLastGameModeEvent((PlayerGameModeChangeEvent) (Object) this);
    }

    @Unique
    public PlayerGameModeChangeEvent.Cause getCause() {
        PlayerGameModeChangeEvent.Cause cause = this.paperarc$cause;
        return cause == null ? PlayerGameModeChangeEvent.Cause.UNKNOWN : cause;
    }

    @Unique
    public Component cancelMessage() {
        return this.paperarc$cancelMessage;
    }

    @Unique
    public void cancelMessage(Component message) {
        this.paperarc$cancelMessage = message;
    }
}
