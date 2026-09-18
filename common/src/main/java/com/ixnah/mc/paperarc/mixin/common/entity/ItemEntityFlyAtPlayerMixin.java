package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code PlayerPickupItemEvent#getFlyAtPlayer()} 的消费方（paper 的
 * PlayerPickupItemEvent-setFlyAtPlayer.patch）。
 *
 * <p>两条路径都要做，否则就是"存了个值没人读"：
 * <ul>
 *   <li>正常拾取：{@code Player#take} 那次动画包按 {@code flyAtPlayer} 决定发不发；</li>
 *   <li>事件被取消：paper 在恢复堆叠数之后、{@code return} 之前，
 *       仍按 {@code flyAtPlayer} 补发一次 {@code take}。Arclight 把
 *       {@code playerTouch} 整个覆写了、取消支里没有这一句，
 *       所以锚在覆写体里唯一一次 {@code PlayerPickupItemEvent#isCancelled()} 上补。</li>
 * </ul>
 *
 * <p>{@code @Local(index = 3)} 的槽号取自 Arclight 编译产物
 * {@code ItemEntityMixin#playerTouch} 的 LocalVariableTable（slot 3 = {@code i}，
 * 拾取前的原始堆叠数，正是 paper 传给 {@code take} 的那个）。1.20.1 只有 Forge
 * 一个加载器，写死 index 安全；回流 main 时不要照搬（checklist bj）。
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityFlyAtPlayerMixin {

    @Inject(method = "playerTouch", at = @At("HEAD"))
    private void paperarc$resetPickupEvent(Player player, CallbackInfo ci) {
        EventCauseState.clearLastPickupEvent();
    }

    @WrapOperation(method = "playerTouch",
            at = @At(value = "INVOKE", remap = false,
                    target = "Lorg/bukkit/event/player/PlayerPickupItemEvent;isCancelled()Z"))
    private boolean paperarc$flyAtPlayerWhenCancelled(PlayerPickupItemEvent event, Operation<Boolean> original,
                                                      Player player, @Local(index = 3) int originalCount) {
        boolean cancelled = original.call(event);
        if (cancelled && event.getFlyAtPlayer()) {
            player.take((ItemEntity) (Object) this, originalCount);
        }
        return cancelled;
    }

    @WrapWithCondition(method = "playerTouch",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;take(Lnet/minecraft/world/entity/Entity;I)V"))
    private boolean paperarc$shouldFlyAtPlayer(Player player, net.minecraft.world.entity.Entity item, int count) {
        PlayerPickupItemEvent event = EventCauseState.getLastPickupEvent();
        return event == null || event.getFlyAtPlayer();
    }
}
