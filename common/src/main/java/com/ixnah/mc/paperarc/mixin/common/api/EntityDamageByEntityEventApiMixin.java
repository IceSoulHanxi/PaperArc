package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Function;
import com.ixnah.mc.paperarc.bridge.api.PaperarcEventCauses;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * paper 的 {@code EntityDamageByEntityEvent#isCritical()}。
 *
 * <p>paper 把"这一击是不是暴击"挂在 NMS {@code DamageSource} 上再传进事件构造器；
 * 运行时的构造点是 spigot 老签名，改成由两个产生暴击的调用点
 * （{@code Player#attack} 的暴击标志、{@code AbstractArrow#onHitEntity} 的
 * {@code isCritArrow()}）在 {@code hurt} 前后压 ThreadLocal，事件构造器取即清。
 *
 * <p>1.21.1 有四个构造器，但其中两个只是委托；只挂两个**终端**构造器
 * （带 {@code org.bukkit.damage.DamageSource} 的那两个，`javap -c` 核对委托链），
 * 否则委托进来时会被内层先取走。
 *
 * <p>嵌套伤害（荆棘反伤等）拿到 {@code false}：外层 {@code hurt} 里第一个构造出来的
 * 伤害事件已经把值取走了，与 paper 的语义一致。
 */
@Mixin(EntityDamageByEntityEvent.class)
public abstract class EntityDamageByEntityEventApiMixin {

    @Unique
    private boolean paperarc$critical;

    @Inject(method = "<init>(Lorg/bukkit/entity/Entity;Lorg/bukkit/entity/Entity;Lorg/bukkit/event/entity/EntityDamageEvent$DamageCause;Lorg/bukkit/damage/DamageSource;D)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$captureCritical(Entity damager, Entity damagee, EntityDamageEvent.DamageCause cause,
                                          DamageSource source, double damage, CallbackInfo ci) {
        this.paperarc$critical = PaperarcEventCauses.takeDamageCritical();
    }

    @Inject(method = "<init>(Lorg/bukkit/entity/Entity;Lorg/bukkit/entity/Entity;Lorg/bukkit/event/entity/EntityDamageEvent$DamageCause;Lorg/bukkit/damage/DamageSource;Ljava/util/Map;Ljava/util/Map;)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$captureCriticalWithModifiers(
            Entity damager, Entity damagee, EntityDamageEvent.DamageCause cause, DamageSource source,
            Map<EntityDamageEvent.DamageModifier, Double> modifiers,
            Map<EntityDamageEvent.DamageModifier, ? extends Function<? super Double, Double>> modifierFunctions,
            CallbackInfo ci) {
        this.paperarc$critical = PaperarcEventCauses.takeDamageCritical();
    }

    @Unique
    public boolean isCritical() {
        return this.paperarc$critical;
    }
}
