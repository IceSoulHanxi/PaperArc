package com.ixnah.mc.paperarc.mixin.common.server;

import com.ixnah.mc.paperarc.bridge.api.PaperarcComponents;
import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.PlayerList;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code PlayerAdvancementDoneEvent#message()} 的消费方。
 *
 * <p>只在 {@code PlayerAdvancements#award} 打开的那个窗口里生效
 * （见 {@code PlayerAdvancementsMessageMixin}）：插件把消息设成 {@code null}
 * 就整条不播（静默完成进度），设成别的就播插件给的那一份。
 *
 * <p>为什么不直接锚广播那句：1.21.1 的广播在 {@code award} 的一个 lambda 里，
 * 合成方法名三端各异、Mixin 的 {@code method} 选择器选不了。
 */
@Mixin(PlayerList.class)
public abstract class PlayerListAdvancementBroadcastMixin {

    @Inject(method = "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V",
            at = @At("HEAD"), cancellable = true)
    private void paperarc$suppressAdvancementMessage(Component message, boolean overlay, CallbackInfo ci) {
        PlayerAdvancementDoneEvent event = paperarc$advancementEvent();
        if (event != null && event.message() == null) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V",
            at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private Component paperarc$replaceAdvancementMessage(Component message) {
        PlayerAdvancementDoneEvent event = paperarc$advancementEvent();
        if (event == null || event.message() == null) {
            return message;
        }
        return PaperarcComponents.toVanilla(event.message());
    }

    private static PlayerAdvancementDoneEvent paperarc$advancementEvent() {
        return PaperarcEventCauses.advancementBroadcasting() ? PaperarcEventCauses.advancementEvent() : null;
    }
}
