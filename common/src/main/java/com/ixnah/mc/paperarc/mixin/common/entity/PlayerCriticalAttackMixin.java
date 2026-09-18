package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.EventCauseState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * {@code EntityDamageByEntityEvent#isCritical()} 在玩家近战这一路的取值来源
 * （paper 的 Add-critical-damage-API.patch）。
 *
 * <p>Arclight 把 {@code Player#attack} 整个 {@code @Overwrite} 掉，暴击判定落在
 * 覆写体自己的局部变量 {@code flag3} 上（还被 Forge 的 {@code CriticalHitEvent} 改写过一次）。
 * {@code @Local(index = 8)} 的槽号是从 Arclight 编译产物
 * {@code PlayerMixin#attack} 的 LocalVariableTable 直接读出来的
 * （`javap -c -p -l`：slot 8 = flag3，作用域覆盖两处 {@code hurt} 调用）。
 * 1.20.1 只有 Forge 一个加载器，写死 index 安全；**回流 main 时不要照搬**（checklist bj）。
 *
 * <p>主命中与横扫各一个 handler：两处 {@code hurt} 的调用 owner 不同
 * （{@code Entity} / {@code LivingEntity}），一个 {@code @WrapOperation} 覆盖不了。
 */
@Mixin(Player.class)
public abstract class PlayerCriticalAttackMixin {

    @WrapOperation(method = "attack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean paperarc$markCriticalHit(Entity target, DamageSource source, float damage,
                                             Operation<Boolean> original, @Local(index = 8) boolean critical) {
        EventCauseState.setDamageCritical(critical);
        try {
            return original.call(target, source, damage);
        } finally {
            EventCauseState.clearDamageCritical();
        }
    }

    @WrapOperation(method = "attack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean paperarc$markCriticalSweep(LivingEntity target, DamageSource source, float damage,
                                               Operation<Boolean> original, @Local(index = 8) boolean critical) {
        EventCauseState.setDamageCritical(critical);
        try {
            return original.call(target, source, damage);
        } finally {
            EventCauseState.clearDamageCritical();
        }
    }
}
