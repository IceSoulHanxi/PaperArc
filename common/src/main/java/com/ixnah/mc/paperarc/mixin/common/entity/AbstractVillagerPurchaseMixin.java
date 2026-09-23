package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.papermc.paper.event.player.PlayerPurchaseEvent;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * {@code PlayerPurchaseEvent#isIncreaseTradeUses()} / {@code isRewardExp()} 的消费方
 * （gaps.md E2 的另一半）。
 *
 * <p>事件本身由 {@code inventory.MerchantResultSlotTradeMixin} 在
 * {@code MerchantResultSlot#onTake} 的 HEAD 派发（那时 {@code checkTakeAchievements}
 * 还没跑，顺序与 paper 一致）；两个开关真正起作用的地方在 {@code AbstractVillager#notifyTrade}：
 * {@code offer.increaseUses()} 与 {@code rewardTradeXp(offer)}（`javap -c` 核对 1.21.1，
 * offset 1 与 15）。不守住这两句，插件把开关关掉也是"设了没反应"。
 */
@Mixin(AbstractVillager.class)
public abstract class AbstractVillagerPurchaseMixin {

    @WrapWithCondition(method = "notifyTrade",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/trading/MerchantOffer;increaseUses()V"))
    private boolean paperarc$shouldIncreaseUses(MerchantOffer offer) {
        PlayerPurchaseEvent event = PaperarcEventCauses.purchaseEvent();
        return event == null || event.willIncreaseTradeUses();
    }

    @WrapWithCondition(method = "notifyTrade",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/npc/AbstractVillager;rewardTradeXp(Lnet/minecraft/world/item/trading/MerchantOffer;)V"))
    private boolean paperarc$shouldRewardExp(AbstractVillager villager, MerchantOffer offer) {
        PlayerPurchaseEvent event = PaperarcEventCauses.purchaseEvent();
        return event == null || event.isRewardingExp();
    }
}
