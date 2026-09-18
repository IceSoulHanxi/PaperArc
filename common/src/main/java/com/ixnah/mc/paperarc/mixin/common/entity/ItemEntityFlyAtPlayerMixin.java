package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code PlayerPickupItemEvent#getFlyAtPlayer()} 的消费方
 * （paper 的 PlayerPickupItemEvent-setFlyAtPlayer.patch）。
 *
 * <p>消费点是 {@code Player#take} 那次"物品飞向玩家"的动画包：
 * {@code setFlyAtPlayer(false)} 就不发。原始堆叠数**不读局部变量**（LVT 槽位跨加载器不通用）：
 * 在 {@code playerTouch} 的 HEAD 直接从 {@code getItem().getCount()} 取，那时还没有任何扣减。
 *
 * <p><b>与 paper 的一处差异（已记 docs/gaps.md）</b>：paper 在事件被取消时仍会按
 * {@code flyAtPlayer} 补发一次 {@code take}；1.21.1 的 Arclight 把
 * {@code PlayerPickupItemEvent#isCancelled()} 那句放在自己的 {@code @Decorate} handler 里
 * （方法名带加载器随机段，选不了，实测 {@code Scanned 0}），这一支锚不到。
 * 影响面极小：{@code setCancelled(true)} 本身就会把 {@code flyAtPlayer} 同步成 false，
 * 只有"取消之后又显式 {@code setFlyAtPlayer(true)}"才看得出区别。
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityFlyAtPlayerMixin {

    @Inject(method = "playerTouch", at = @At("HEAD"))
    private void paperarc$resetPickupEvent(Player player, CallbackInfo ci) {
        PaperarcEventCauses.popPickupEvent();
        PaperarcEventCauses.pushPickupOriginalCount(((ItemEntity) (Object) this).getItem().getCount());
    }

    @WrapWithCondition(method = "playerTouch",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;take(Lnet/minecraft/world/entity/Entity;I)V"))
    private boolean paperarc$shouldFlyAtPlayer(Player player, net.minecraft.world.entity.Entity item, int count) {
        PlayerPickupItemEvent event = PaperarcEventCauses.pickupEvent();
        return event == null || event.getFlyAtPlayer();
    }
}
