package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * {@code EntityDamageByEntityEvent#isCritical()} 在玩家近战这一路的取值来源
 * （paper 的 Add-critical-damage-API.patch）。
 *
 * <p>暴击标志是 {@code attack} 里的一个局部变量，只能用 MixinExtras 的 {@code @Local} 取。
 * <b>用 {@code ordinal} 而不是 {@code index}</b>：LVT 槽位跨加载器不通用
 * （docs/mixin-conventions.md bl）。1.21.1 vanilla 的 `javap -c -l` 显示，
 * 在那次 {@code Entity#hurt}（offset 458）处在作用域里的 boolean 依次是
 * slot 7 {@code bl}、slot 8 {@code bl2}、slot 9 {@code bl3}、slot 11 {@code bl4}，
 * 暴击标志是 {@code bl3} → {@code ordinal = 2}；Forge/NeoForge 的 {@code CriticalHitEvent}
 * 补丁只是回写同一个 {@code bl3} 并多出一个**对象**局部，不改 boolean 的相对次序
 * （三端 `.mixin.out` 核对）。
 *
 * <p>只挂主命中这一处：横扫那次 {@code LivingEntity#hurt} 在 vanilla 里以
 * {@code !bl3} 为前提（横扫与暴击互斥），主命中的 {@code finally} 已经把值清掉，
 * 横扫拿到的就是默认的 {@code false} —— 与 paper 一致。
 */
@Mixin(Player.class)
public abstract class PlayerCriticalAttackMixin {

    @WrapOperation(method = "attack",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean paperarc$markCriticalHit(Entity target, DamageSource source, float damage,
                                             Operation<Boolean> original, @Local(ordinal = 2) boolean critical) {
        PaperarcEventCauses.pushDamageCritical(critical);
        try {
            return original.call(target, source, damage);
        } finally {
            PaperarcEventCauses.popDamageCritical();
        }
    }
}
