package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
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
 * paper 的 {@code PlayerGameModeChangeEvent#getCause()} 与 {@code cancelMessage()} 一对
 * （{@code additions-to-PlayerGameModeChangeEvent.patch}）。
 *
 * <p>原因由上游调用点压进 {@link PaperarcEventCauses}（插件 / {@code /gamemode} /
 * {@code /defaultgamemode}），构造器读回；同时把事件本身记进 ThreadLocal，供
 * {@code player.ServerPlayerGameModeCauseMixin} 在 {@code setGameMode} 返回 false 时
 * 把 {@code cancelMessage()} 发给玩家 —— 没有这一步，setter 就是个没人读的字段。
 */
@Mixin(PlayerGameModeChangeEvent.class)
public abstract class PlayerGameModeChangeEventApiMixin {

    @Unique
    private PlayerGameModeChangeEvent.Cause paperarc$cause = PlayerGameModeChangeEvent.Cause.UNKNOWN;

    @Unique
    private Component paperarc$cancelMessage;

    @Inject(method = "<init>(Lorg/bukkit/entity/Player;Lorg/bukkit/GameMode;)V", at = @At("RETURN"))
    private void paperarc$captureCause(Player player, GameMode newGameMode, CallbackInfo ci) {
        this.paperarc$cause = PaperarcEventCauses.gameMode();
        PaperarcEventCauses.rememberGameModeEvent((PlayerGameModeChangeEvent) (Object) this);
    }

    @Unique
    public PlayerGameModeChangeEvent.Cause getCause() {
        return this.paperarc$cause;
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
