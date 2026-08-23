package dev.paperarc.mixin.common.inventory;

import io.papermc.paper.event.player.PlayerPurchaseEvent;
import io.papermc.paper.event.player.PlayerTradeEvent;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.paperarc.bridge.PaperArcBridge;
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
 * Deviation from Paper (documented): checkTakeAchievements runs BEFORE the
 * event instead of after it (vanilla order preserved on the success path).
 */
@Mixin(MerchantResultSlot.class)
public abstract class MerchantResultSlotTradeMixin {

    @Shadow
    @Final
    private MerchantContainer slots;

    @Shadow
    @Final
    private Merchant merchant;

    @Unique
    private MerchantOffer paperarc$bukkitTradeOffer;

    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    private void paperarc$playerTrade(Player who, ItemStack stack, CallbackInfo ci) {
        this.paperarc$bukkitTradeOffer = null;
        MerchantOffer offer = this.slots.getActiveOffer();
        if (offer == null || !(who instanceof ServerPlayer serverPlayer)) {
            return;
        }

        io.papermc.paper.event.player.PlayerPurchaseEvent event;
        org.bukkit.inventory.MerchantRecipe recipe = new CraftMerchantRecipe(offer);
        if (this.merchant instanceof net.minecraft.world.entity.npc.AbstractVillager villager) {
            event = new PlayerTradeEvent(
                    PaperArcBridge.bukkitPlayer(serverPlayer),
                    PaperArcBridge.<org.bukkit.entity.AbstractVillager>bukkitEntity(villager),
                    recipe, true, true);
        } else if (this.merchant instanceof CraftMerchantCustom.MinecraftMerchant customMerchant) {
            event = new PlayerPurchaseEvent(
                    PaperArcBridge.bukkitPlayer(serverPlayer),
                    recipe, false, true);
        } else {
            return;
        }

        PaperArcBridge.fire(event);
        if (event.isCancelled()) {
            stack.setCount(0);
            ((org.bukkit.entity.Player) PaperArcBridge.bukkitPlayer(serverPlayer)).updateInventory();
            ci.cancel();
        } else {
            this.paperarc$bukkitTradeOffer =
                    CraftMerchantRecipe.fromBukkit(event.getTrade()).toMinecraft();
        }
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
