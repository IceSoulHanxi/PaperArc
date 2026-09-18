package com.ixnah.mc.paperarc.mixin.common.server;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.KickCommand;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.event.player.PlayerKickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** {@code /kick} 的 {@code PlayerKickEvent.Cause.KICK_COMMAND}（理由是任意文本，翻译键反查不出来）。 */
@Mixin(KickCommand.class)
public abstract class KickCommandCauseMixin {

    @WrapOperation(method = "kickPlayers",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;disconnect(Lnet/minecraft/network/chat/Component;)V"))
    private static void paperarc$kickCommandCause(ServerGamePacketListenerImpl listener, Component reason,
                                                  Operation<Void> original) {
        PaperarcEventCauses.pushKick(PlayerKickEvent.Cause.KICK_COMMAND);
        try {
            original.call(listener, reason);
        } finally {
            PaperarcEventCauses.popKick();
        }
    }
}
