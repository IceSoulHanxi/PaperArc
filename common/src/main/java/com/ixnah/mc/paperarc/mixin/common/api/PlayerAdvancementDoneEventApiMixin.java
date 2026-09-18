package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import net.kyori.adventure.text.Component;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code PlayerAdvancementDoneEvent#message()/message(Component)}。
 *
 * <p>默认值按 paper 的公式在 {@code PlayerAdvancements#award} 的 HEAD 算好
 * （{@code shouldAnnounceChat()} 为假时是 {@code null}），压进 {@link EventCauseState}；
 * 事件构造器取回，并把自己登记成"最近一次"，好让广播那一步读回可能被插件改过的值
 * （消费方见 {@code PlayerAdvancementsMessageMixin}）。
 */
@Mixin(PlayerAdvancementDoneEvent.class)
public abstract class PlayerAdvancementDoneEventApiMixin {

    @Unique
    private Component paperarc$message;

    @Inject(method = "<init>(Lorg/bukkit/entity/Player;Lorg/bukkit/advancement/Advancement;)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$captureMessage(Player player, Advancement advancement, CallbackInfo ci) {
        this.paperarc$message = EventCauseState.takeAdvancementMessage();
        EventCauseState.setLastAdvancementEvent((PlayerAdvancementDoneEvent) (Object) this);
    }

    @Unique
    public Component message() {
        return this.paperarc$message;
    }

    @Unique
    public void message(Component message) {
        this.paperarc$message = message;
    }
}
