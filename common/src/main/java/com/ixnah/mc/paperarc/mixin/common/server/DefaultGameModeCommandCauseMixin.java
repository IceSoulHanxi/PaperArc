package com.ixnah.mc.paperarc.mixin.common.server;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.DefaultGameModeCommands;
import net.minecraft.world.level.GameType;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** {@code /defaultgamemode} 的 {@code PlayerGameModeChangeEvent.Cause.DEFAULT_GAMEMODE}。 */
@Mixin(DefaultGameModeCommands.class)
public abstract class DefaultGameModeCommandCauseMixin {

    @Inject(method = "setMode", at = @At("HEAD"))
    private static void paperarc$pushDefaultCause(CommandSourceStack source, GameType gameType,
                                                  CallbackInfoReturnable<Integer> cir) {
        PaperarcEventCauses.pushGameMode(PlayerGameModeChangeEvent.Cause.DEFAULT_GAMEMODE);
    }

    @Inject(method = "setMode", at = @At("RETURN"))
    private static void paperarc$popDefaultCause(CommandSourceStack source, GameType gameType,
                                                 CallbackInfoReturnable<Integer> cir) {
        PaperarcEventCauses.popGameMode();
    }
}
