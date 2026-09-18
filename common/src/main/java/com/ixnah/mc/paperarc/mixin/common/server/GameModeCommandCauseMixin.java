package com.ixnah.mc.paperarc.mixin.common.server;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

/** {@code /gamemode} 的 {@code PlayerGameModeChangeEvent.Cause.COMMAND}。 */
@Mixin(GameModeCommand.class)
public abstract class GameModeCommandCauseMixin {

    @Inject(method = "setMode", at = @At("HEAD"))
    private static void paperarc$pushCommandCause(CommandContext<CommandSourceStack> context,
                                                  Collection<ServerPlayer> targets, GameType gameType,
                                                  CallbackInfoReturnable<Integer> cir) {
        PaperarcEventCauses.pushGameMode(PlayerGameModeChangeEvent.Cause.COMMAND);
    }

    @Inject(method = "setMode", at = @At("RETURN"))
    private static void paperarc$popCommandCause(CommandContext<CommandSourceStack> context,
                                                 Collection<ServerPlayer> targets, GameType gameType,
                                                 CallbackInfoReturnable<Integer> cir) {
        PaperarcEventCauses.popGameMode();
    }
}
