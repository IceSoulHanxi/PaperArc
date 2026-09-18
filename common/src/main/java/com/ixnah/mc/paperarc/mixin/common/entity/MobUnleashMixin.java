package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code EntityUnleashEvent} 的取消与 {@code isDropLeash()} 在 {@code Mob} 侧的消费方。
 *
 * <p>Arclight 在这四个方法里各 {@code @Inject} 一次事件，紧接着就是
 * {@code dropLeash(sendPacket, dropLead)}；把这次调用 {@code @WrapOperation} 掉：
 * 取消 → 整句不执行（与 paper 的 {@code return} 等效，那句之后没有别的动作）；
 * 否则第二个实参换成事件上的 {@code isDropLeash()}。
 *
 * <p>{@code removeAfterChangingDimensions} 与 {@code interact}（玩家手持空手解绳）
 * 的默认 dropLeash 与其它两处不同（paper 分别传 {@code false} 与 {@code !instabuild}），
 * 在 HEAD 压进 {@link EventCauseState}。
 */
@Mixin(Mob.class)
public abstract class MobUnleashMixin {

    @Inject(method = {"tickLeash", "startRiding"}, at = @At("HEAD"))
    private void paperarc$resetUnleashEvent(CallbackInfo ci) {
        EventCauseState.clearLastUnleashEvent();
    }

    @Inject(method = "removeAfterChangingDimensions", at = @At("HEAD"))
    private void paperarc$dimensionUnleashDefault(CallbackInfo ci) {
        EventCauseState.clearLastUnleashEvent();
        EventCauseState.setUnleashDropLeash(false);
    }

    @Inject(method = "interact", at = @At("HEAD"))
    private void paperarc$playerUnleashDefault(Player player, InteractionHand hand,
                                               CallbackInfoReturnable<?> cir) {
        EventCauseState.clearLastUnleashEvent();
        EventCauseState.setUnleashDropLeash(!player.getAbilities().instabuild);
    }

    @WrapOperation(method = {"tickLeash", "startRiding", "removeAfterChangingDimensions", "interact"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;dropLeash(ZZ)V"))
    private void paperarc$applyUnleashEvent(Mob mob, boolean sendPacket, boolean dropLead,
                                            Operation<Void> original) {
        EntityUnleashEvent event = EventCauseState.getLastUnleashEvent();
        if (event == null) {
            original.call(mob, sendPacket, dropLead);
            return;
        }
        if (event.isCancelled()) {
            return;
        }
        original.call(mob, sendPacket, event.isDropLeash());
    }
}
