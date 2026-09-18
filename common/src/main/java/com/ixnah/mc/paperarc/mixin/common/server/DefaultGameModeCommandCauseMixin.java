package com.ixnah.mc.paperarc.mixin.common.server;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.commands.DefaultGameModeCommands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** {@code PlayerGameModeChangeEvent#getCause} 的 {@code DEFAULT_GAMEMODE} 来源（{@code /defaultgamemode}）。 */
@Mixin(DefaultGameModeCommands.class)
public abstract class DefaultGameModeCommandCauseMixin {

    @WrapOperation(
            method = "setMode",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;setGameMode(Lnet/minecraft/world/level/GameType;)Z"))
    private static boolean paperarc$defaultGameModeCause(ServerPlayer player, GameType gameType,
                                                         Operation<Boolean> original) {
        EventCauseState.setGameModeCause(PlayerGameModeChangeEvent.Cause.DEFAULT_GAMEMODE);
        try {
            return original.call(player, gameType);
        } finally {
            EventCauseState.clearGameModeCause();
        }
    }
}
