package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * paper 的 {@code EntityRegainHealthEvent#isFastRegen()}。
 *
 * <p>paper 只在一个地方传 {@code true}：{@code FoodData#tick} 里
 * "饱和度 &gt; 0 的快速回血"那一支（另一支 80 tick 一次的慢回血传 {@code false}）。
 * 两支在 1.20.1 调的都是 {@code Player#heal(float)}（CraftBukkit 的
 * {@code heal(float, RegainReason)} 重载在 Forge 侧不存在），
 * 靠 {@code FoodDataFastRegenMixin} 的 slice 区分，再压进 {@link EventCauseState}。
 */
@Mixin(EntityRegainHealthEvent.class)
public abstract class EntityRegainHealthEventApiMixin {

    @Unique
    private boolean paperarc$fastRegen;

    @Inject(method = "<init>(Lorg/bukkit/entity/Entity;DLorg/bukkit/event/entity/EntityRegainHealthEvent$RegainReason;)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$captureFastRegen(Entity entity, double amount,
                                           EntityRegainHealthEvent.RegainReason regainReason, CallbackInfo ci) {
        this.paperarc$fastRegen = EventCauseState.takeFastRegen();
    }

    @Unique
    public boolean isFastRegen() {
        return this.paperarc$fastRegen;
    }
}
