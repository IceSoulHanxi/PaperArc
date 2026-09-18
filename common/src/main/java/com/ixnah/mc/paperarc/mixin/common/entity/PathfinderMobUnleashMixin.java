package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * {@code EntityUnleashEvent} 在"绳子拉太远自动断"这一路（{@code PathfinderMob#tickLeash}）的消费方。
 *
 * <p>paper 在这里取消时是直接 {@code return}，也就是连后面那句
 * {@code goalSelector.disableControlFlag(Goal.Flag.MOVE)} 一起跳过，
 * 所以除了 {@code dropLeash} 还要把它也守住。
 */
@Mixin(PathfinderMob.class)
public abstract class PathfinderMobUnleashMixin {

    @Inject(method = "tickLeash", at = @At("HEAD"))
    private void paperarc$resetUnleashEvent(CallbackInfo ci) {
        EventCauseState.clearLastUnleashEvent();
    }

    @WrapOperation(method = "tickLeash",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/PathfinderMob;dropLeash(ZZ)V"))
    private void paperarc$applyUnleashEvent(PathfinderMob mob, boolean sendPacket, boolean dropLead,
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

    @WrapWithCondition(method = "tickLeash",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/goal/GoalSelector;disableControlFlag(Lnet/minecraft/world/entity/ai/goal/Goal$Flag;)V"))
    private boolean paperarc$skipDisableMoveWhenCancelled(GoalSelector selector, Goal.Flag flag) {
        EntityUnleashEvent event = EventCauseState.getLastUnleashEvent();
        return event == null || !event.isCancelled();
    }
}
