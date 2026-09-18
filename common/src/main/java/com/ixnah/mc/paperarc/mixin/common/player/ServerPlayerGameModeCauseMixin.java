package com.ixnah.mc.paperarc.mixin.common.player;

import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code PlayerGameModeChangeEvent#cancelMessage()} 的兑现点：Paper 在事件被取消后
 * 把这条消息发给玩家。Arclight 在 {@code ServerPlayerGameMode#changeGameModeForPlayer}
 * 里派发事件、取消时返回 false，一路传回 {@code ServerPlayer#setGameMode}；
 * 我们在这里收口（事件对象由它自己的构造器记进 ThreadLocal）。
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerGameModeCauseMixin {

    @Inject(method = "setGameMode", at = @At("HEAD"))
    private void paperarc$clearGameModeEvent(GameType gameType, CallbackInfoReturnable<Boolean> cir) {
        PaperarcEventCauses.takeGameModeEvent();
    }

    @Inject(method = "setGameMode", at = @At("RETURN"))
    private void paperarc$sendCancelMessage(GameType gameType, CallbackInfoReturnable<Boolean> cir) {
        PlayerGameModeChangeEvent event = PaperarcEventCauses.takeGameModeEvent();
        if (event == null || Boolean.TRUE.equals(cir.getReturnValue()) || event.cancelMessage() == null) {
            return;
        }
        org.bukkit.entity.Entity bukkit = PaperArcBridge.bukkitEntity((ServerPlayer) (Object) this);
        if (bukkit instanceof org.bukkit.entity.Player player) {
            player.sendMessage(event.cancelMessage());
        }
    }
}
