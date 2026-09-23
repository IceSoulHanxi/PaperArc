package com.ixnah.mc.paperarc.mixin.common.server;

import com.ixnah.mc.paperarc.bridge.api.PaperarcComponents;
import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code PlayerAdvancementDoneEvent#message()} 的取值来源与广播窗口
 * （paper 的 Add-Adventure-message-to-PlayerAdvancementDoneEvent.patch）。
 *
 * <p>① HEAD：按 vanilla 的公式算出默认广播文本（不该播报的进度是 {@code null}），
 * 压 ThreadLocal 等事件构造器取走。Arclight 在 {@code AdvancementRewards#grant} 处
 * 才 fire 事件，一定晚于 HEAD。
 *
 * <p>② {@code Optional#ifPresent} 那句之后打开"广播窗口"：**1.21.1 把广播挪进了
 * 一个 lambda**（`javap`：{@code award} 里是 {@code display().ifPresent(this::…)}，
 * 真正的 {@code broadcastSystemMessage} 在合成方法 {@code method_53637} 里），
 * 而 lambda 的方法名三端各异、选不了。所以消费方改挂在
 * {@code PlayerList#broadcastSystemMessage} 上，用这个窗口把它和别的广播区分开
 * （见 {@code PlayerListAdvancementBroadcastMixin}）。
 */
@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsMessageMixin {

    @Shadow
    private ServerPlayer player;

    @Inject(method = "award", at = @At("HEAD"))
    private void paperarc$prepareAdvancementMessage(AdvancementHolder advancement, String criterion,
                                                    CallbackInfoReturnable<Boolean> cir) {
        DisplayInfo display = advancement.value().display().orElse(null);
        if (display == null || !display.shouldAnnounceChat()
                || !this.player.level().getGameRules().get(GameRules.SHOW_ADVANCEMENT_MESSAGES)) {
            PaperarcEventCauses.pushAdvancementMessage(null);
            return;
        }
        PaperarcEventCauses.pushAdvancementMessage(PaperarcComponents.fromVanilla(
                display.getType().createAnnouncement(advancement, this.player)));
    }

    @Inject(method = "award",
            at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V",
                    remap = false))
    private void paperarc$openAdvancementBroadcast(AdvancementHolder advancement, String criterion,
                                                   CallbackInfoReturnable<Boolean> cir) {
        PaperarcEventCauses.openAdvancementBroadcast();
    }

    @Inject(method = "award", at = @At("RETURN"))
    private void paperarc$clearAdvancementMessage(AdvancementHolder advancement, String criterion,
                                                  CallbackInfoReturnable<Boolean> cir) {
        PaperarcEventCauses.closeAdvancementBroadcast();
        PaperarcEventCauses.popAdvancementMessage();
        PaperarcEventCauses.popAdvancementEvent();
    }
}
