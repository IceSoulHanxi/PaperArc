package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import io.papermc.paper.event.player.PlayerPurchaseEvent;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * {@code PlayerPurchaseEvent#willIncreaseTradeUses()} / {@code isRewardingExp()} 的消费方
 * （gaps.md E2 的后半；paper 的 Add-PlayerTradeEvent-and-PlayerPurchaseEvent.patch）。
 *
 * <p>paper 把 {@code increaseUses()} 与 {@code rewardTradeXp()} 从
 * {@code AbstractVillager#notifyTrade} 提到新方法 {@code processTrade(offer, event)} 里，
 * 按事件上的两个开关决定做不做；mixin 加不了方法形参，改成在 {@code notifyTrade} 里
 * 直接守住这两次调用（事件由 {@code MerchantResultSlotTradeMixin} 压进
 * {@link EventCauseState}，不是交易触发的调用拿到 {@code null}、行为与 vanilla 相同）。
 */
@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerTradeMixin {

    @WrapWithCondition(method = "notifyTrade",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/trading/MerchantOffer;increaseUses()V"))
    private boolean paperarc$shouldIncreaseUses(MerchantOffer offer) {
        PlayerPurchaseEvent event = EventCauseState.getLastPurchaseEvent();
        return event == null || event.willIncreaseTradeUses();
    }

    @WrapWithCondition(method = "notifyTrade",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/npc/AbstractVillager;rewardTradeXp(Lnet/minecraft/world/item/trading/MerchantOffer;)V"))
    private boolean paperarc$shouldRewardExp(AbstractVillager villager, MerchantOffer offer) {
        PlayerPurchaseEvent event = EventCauseState.getLastPurchaseEvent();
        return event == null || event.isRewardingExp();
    }
}
