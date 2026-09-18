package com.ixnah.mc.paperarc.mixin.common.inventory;

import io.papermc.paper.event.player.PlayerPurchaseEvent;
import io.papermc.paper.event.player.PlayerTradeEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantContainer;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import org.bukkit.craftbukkit.v.inventory.CraftMerchantCustom;
import org.bukkit.craftbukkit.v.inventory.CraftMerchantRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Port of Paper's PlayerTradeEvent / PlayerPurchaseEvent for villager trades
 * (Add-PlayerTradeEvent-and-PlayerPurchaseEvent.patch).
 *
 * Paper injects into MerchantResultSlot.onTake right after the active offer is
 * resolved: fires the event (PlayerTradeEvent for NMS AbstractVillager,
 * PlayerPurchaseEvent(rewardExp=false) for CraftMerchantCustom.MinecraftMerchant);
 * on cancel: stack.setCount(0) + updateInventory() + return (skipping the whole
 * trade); otherwise replaces the offer used downstream with
 * CraftMerchantRecipe.fromBukkit(event.getTrade()).toMinecraft().
 *
 * Implementation: HEAD injection fires the event and cancels on cancellation;
 * a WrapOperation on Merchant.notifyTrade substitutes the stored offer so the
 * modified trade applies to vanilla processing.
 *
 * <p>A8/Y-4（gaps.md E2）复核过顺序：{@code checkTakeAchievements(stack)} 是
 * {@code onTake} 的**第一句**（`javap -c` 核对 forge 映射后的
 * {@code MerchantResultSlot#onTake}，offset 2），而我们的事件挂在 {@code @At("HEAD")}，
 * 也就是**在它之前**触发、取消时 {@code ci.cancel()} 连它一起跳过 ——
 * 与 paper 把那一句挪到事件之后的效果一致，原先记的"顺序有偏差"是错的。
 *
 * <p>同时补上 {@code PlayerPurchaseEvent} 的两个开关
 * （{@code willIncreaseTradeUses()} / {@code isRewardingExp()}）：
 * paper 是把 {@code increaseUses}/{@code rewardTradeXp} 从
 * {@code notifyTrade} 提到新方法 {@code processTrade(offer, event)} 里按开关执行；
 * 我们改成把事件压进 {@link com.ixnah.mc.paperarc.bridge.EventCauseState}，
 * 由 {@code AbstractVillagerTradeMixin} 在 {@code notifyTrade} 里读回。
 */
@Mixin(MerchantResultSlot.class)
public abstract class MerchantResultSlotTradeMixin {

    @Accessor("slots")
    abstract MerchantContainer paperarc$getSlots();

    @Accessor("merchant")
    abstract Merchant paperarc$getMerchant();

    @Unique
    private MerchantOffer paperarc$bukkitTradeOffer;

    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    private void paperarc$playerTrade(Player who, ItemStack stack, CallbackInfo ci) {
        this.paperarc$bukkitTradeOffer = null;
        MerchantOffer offer = this.paperarc$getSlots().getActiveOffer();
        if (offer == null || !(who instanceof ServerPlayer serverPlayer)) {
            return;
        }

        io.papermc.paper.event.player.PlayerPurchaseEvent event;
        org.bukkit.inventory.MerchantRecipe recipe = new CraftMerchantRecipe(offer);
        if (this.paperarc$getMerchant() instanceof net.minecraft.world.entity.npc.AbstractVillager villager) {
            event = new PlayerTradeEvent(
                    PaperArcBridge.bukkitPlayer(serverPlayer),
                    PaperArcBridge.<org.bukkit.entity.AbstractVillager>bukkitEntity(villager),
                    recipe, true, true);
        } else if (this.paperarc$getMerchant() instanceof CraftMerchantCustom.MinecraftMerchant customMerchant) {
            event = new PlayerPurchaseEvent(
                    PaperArcBridge.bukkitPlayer(serverPlayer),
                    recipe, false, true);
        } else {
            return;
        }

        PaperArcBridge.fire(event);
        com.ixnah.mc.paperarc.bridge.EventCauseState.setLastPurchaseEvent(event);
        if (event.isCancelled()) {
            stack.setCount(0);
            ((org.bukkit.entity.Player) PaperArcBridge.bukkitPlayer(serverPlayer)).updateInventory();
            ci.cancel();
        } else {
            this.paperarc$bukkitTradeOffer =
                    CraftMerchantRecipe.fromBukkit(event.getTrade()).toMinecraft();
        }
    }

    @Inject(method = "onTake", at = @At("RETURN"))
    private void paperarc$clearPurchaseEvent(Player who, ItemStack stack, CallbackInfo ci) {
        com.ixnah.mc.paperarc.bridge.EventCauseState.clearLastPurchaseEvent();
    }

    @WrapOperation(
            method = "onTake",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/trading/Merchant;notifyTrade(Lnet/minecraft/world/item/trading/MerchantOffer;)V"
            )
    )
    private void paperarc$substituteOffer(Merchant merchant, MerchantOffer offer, Operation<Void> original) {
        original.call(merchant,
                this.paperarc$bukkitTradeOffer != null ? this.paperarc$bukkitTradeOffer : offer);
        this.paperarc$bukkitTradeOffer = null;
    }
}
