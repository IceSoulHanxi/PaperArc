package com.ixnah.mc.paperarc.mixin.common.player;

import com.ixnah.mc.paperarc.bridge.ServerPlayerQuitReasonBridge;
import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 两件事都挂在同一个汇聚点 {@code ServerCommonPacketListenerImpl#disconnect(DisconnectionDetails)}：
 *
 * <ul>
 *   <li>Paper {@code Add-API-for-quit-reason.patch}：被踢的退出原因是 {@code KICKED}；</li>
 *   <li>Paper {@code Add-PlayerKickEvent-causes.patch}：Paper 给三十来个 {@code disconnect(...)}
 *       调用点各传一个 Cause，那些点大多落在 Arclight 改不动/锚不住的位置；这里改成在这个
 *       唯一汇聚点按**原版翻译键**反查（见
 *       {@code PaperarcEventCauses#causeFromDisconnectReason}）。插件的 PLUGIN 与
 *       {@code /kick} 的 KICK_COMMAND 由各自更靠外的锚点先压进 ThreadLocal，这里不覆盖。</li>
 * </ul>
 */
@Mixin(ServerCommonPacketListenerImpl.class)
public abstract class ServerCommonPacketListenerKickMixin {

    @Inject(method = "disconnect(Lnet/minecraft/network/DisconnectionDetails;)V", at = @At("HEAD"))
    private void paperarc$markKick(DisconnectionDetails details, CallbackInfo ci) {
        if ((Object) this instanceof ServerGamePacketListenerImpl listener) {
            ((ServerPlayerQuitReasonBridge) listener.player)
                    .paperarc$setQuitReason(PlayerQuitEvent.QuitReason.KICKED);
        }
        if (PaperarcEventCauses.kick() == org.bukkit.event.player.PlayerKickEvent.Cause.UNKNOWN) {
            PaperarcEventCauses.pushKick(PaperarcEventCauses.causeFromDisconnectReason(details.reason()));
        }
    }
}
