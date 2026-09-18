package com.ixnah.mc.paperarc.mixin.common.server;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import com.ixnah.mc.paperarc.bridge.PaperarcAdventure;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code PlayerAdvancementDoneEvent#message()} 的取值来源与消费方
 * （paper 的 Add-Adventure-message-to-PlayerAdvancementDoneEvent.patch）。
 *
 * <p>① HEAD：按 paper 的公式算出默认广播文本（不该播报的进度是 {@code null}），
 * 压进 {@link EventCauseState} 等事件构造器取走。
 * 用 HEAD 而不是与 Arclight 同一个锚点（{@code Advancement#getRewards()}）——
 * 同一条指令上两个 {@code @Inject} 的先后没有保证，而事件必须在我们压值之后才构造。
 *
 * <p>② {@code @WrapOperation} 广播调用：读回事件上的 {@code message()}，
 * 为 {@code null} 就不播（插件把消息清掉 = 静默完成），否则播插件给的那一份。
 */
@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsMessageMixin {

    @Shadow
    private ServerPlayer player;

    @Inject(method = "award", at = @At("HEAD"))
    private void paperarc$prepareAdvancementMessage(Advancement advancement, String criterion,
                                                    CallbackInfoReturnable<Boolean> cir) {
        DisplayInfo display = advancement.getDisplay();
        if (display == null || !display.shouldAnnounceChat()) {
            EventCauseState.setAdvancementMessage(null);
            return;
        }
        EventCauseState.setAdvancementMessage(PaperarcAdventure.asAdventure(
                Component.translatable("chat.type.advancement." + display.getFrame().getName(),
                        this.player.getDisplayName(), advancement.getChatComponent())));
    }

    @Inject(method = "award", at = @At("RETURN"))
    private void paperarc$clearAdvancementMessage(Advancement advancement, String criterion,
                                                  CallbackInfoReturnable<Boolean> cir) {
        EventCauseState.clearAdvancementMessage();
        EventCauseState.clearLastAdvancementEvent();
    }

    @WrapOperation(method = "award",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"))
    private void paperarc$broadcastEventMessage(PlayerList playerList, Component message, boolean overlay,
                                                Operation<Void> original) {
        org.bukkit.event.player.PlayerAdvancementDoneEvent event = EventCauseState.takeLastAdvancementEvent();
        if (event == null) {
            original.call(playerList, message, overlay);
            return;
        }
        net.kyori.adventure.text.Component adventure = event.message();
        if (adventure == null) {
            return;
        }
        original.call(playerList, PaperarcAdventure.asVanilla(adventure), overlay);
    }
}
