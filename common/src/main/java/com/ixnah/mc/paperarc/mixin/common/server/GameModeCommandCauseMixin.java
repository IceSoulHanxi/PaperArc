package com.ixnah.mc.paperarc.mixin.common.server;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Paper 的 additions-to-PlayerGameModeChangeEvent 在 {@code /gamemode} 这一侧：
 * Cause = {@code COMMAND}，事件被取消且插件设了 {@code cancelMessage()} 时把它发给命令发送者。
 *
 * <p>Arclight 的 {@code changeGameModeForPlayer} 只返回 boolean，拿不到事件对象，
 * 所以事件构造器把自己挂进 {@link EventCauseState} 的 "最近一次" 槽，这里取回来读
 * {@code cancelMessage()}。
 */
@Mixin(GameModeCommand.class)
public abstract class GameModeCommandCauseMixin {

    @WrapOperation(
            method = "setMode",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;setGameMode(Lnet/minecraft/world/level/GameType;)Z"))
    private static boolean paperarc$commandGameModeCause(
            ServerPlayer player, GameType gameType, Operation<Boolean> original,
            @Local(argsOnly = true) CommandContext<CommandSourceStack> context) {
        EventCauseState.setGameModeCause(PlayerGameModeChangeEvent.Cause.COMMAND);
        EventCauseState.clearLastGameModeEvent();
        boolean changed;
        try {
            changed = original.call(player, gameType);
        } finally {
            EventCauseState.clearGameModeCause();
        }
        PlayerGameModeChangeEvent event = EventCauseState.takeLastGameModeEvent();
        if (!changed && event != null && event.cancelMessage() != null) {
            Component message = net.minecraft.network.chat.Component.literal(
                    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                            .serialize(event.cancelMessage()));
            context.getSource().sendSuccess(() -> message, true);
        }
        return changed;
    }
}
