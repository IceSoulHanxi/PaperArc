package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * {@code EntityDamageByEntityEvent#isCritical()} 在箭矢这一路的取值来源：
 * paper 在 {@code AbstractArrow#onHitEntity} 里对 {@code isCritArrow()} 的箭
 * 把伤害源标成暴击。这里不需要读局部变量，判据挂在实体自己身上，三端一致。
 */
@Mixin(AbstractArrow.class)
public abstract class AbstractArrowCriticalMixin {

    @WrapOperation(method = "onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean paperarc$markCriticalArrow(Entity target, DamageSource source, float damage,
                                               Operation<Boolean> original) {
        PaperarcEventCauses.pushDamageCritical(((AbstractArrow) (Object) this).isCritArrow());
        try {
            return original.call(target, source, damage);
        } finally {
            PaperarcEventCauses.popDamageCritical();
        }
    }
}
