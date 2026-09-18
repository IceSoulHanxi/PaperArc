package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
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
 * <p>paper 只在一个地方传 {@code true}：{@code FoodData#tick} 里"饱和度 &gt; 0 的快速回血"
 * 那一支（另一支 80 tick 一次的慢回血传 {@code false}）。两支调的都是
 * {@code Player#heal(float)}，靠 {@code entity.FoodDataFastRegenMixin} 的 slice 区分。
 */
@Mixin(EntityRegainHealthEvent.class)
public abstract class EntityRegainHealthEventApiMixin {

    @Unique
    private boolean paperarc$fastRegen;

    @Inject(method = "<init>(Lorg/bukkit/entity/Entity;DLorg/bukkit/event/entity/EntityRegainHealthEvent$RegainReason;)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$captureFastRegen(Entity entity, double amount,
                                           EntityRegainHealthEvent.RegainReason regainReason, CallbackInfo ci) {
        this.paperarc$fastRegen = PaperarcEventCauses.takeFastRegen();
    }

    @Unique
    public boolean isFastRegen() {
        return this.paperarc$fastRegen;
    }
}
