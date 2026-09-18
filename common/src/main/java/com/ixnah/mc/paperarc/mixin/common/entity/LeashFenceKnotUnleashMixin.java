package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code EntityUnleashEvent#isDropLeash()} 在"打拴绳结"这一路的消费方
 * （Arclight 的 {@code LeashFenceKnotEntityMixin#interact} 覆写体里，
 * 事件是 {@code PlayerUnleashEntityEvent}，取消由 Arclight 自己处理）。
 */
@Mixin(LeashFenceKnotEntity.class)
public abstract class LeashFenceKnotUnleashMixin {

    @Inject(method = "interact", at = @At("HEAD"))
    private void paperarc$playerUnleashDefault(Player player, InteractionHand hand,
                                               CallbackInfoReturnable<?> cir) {
        EventCauseState.clearLastUnleashEvent();
        EventCauseState.setUnleashDropLeash(!player.getAbilities().instabuild);
    }

    @WrapOperation(method = "interact",
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
