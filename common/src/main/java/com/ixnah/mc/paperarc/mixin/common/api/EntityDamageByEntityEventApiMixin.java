package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Function;
import com.ixnah.mc.paperarc.bridge.EventCauseState;
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
 * <p>paper 把"这一击是不是暴击"挂在 NMS {@code DamageSource} 上再传进事件构造器。
 * Arclight 的构造点是 spigot 老签名，改成由两个产生暴击的调用点
 * （{@code Player#attack} 的 {@code flag3}、{@code AbstractArrow#onHitEntity} 的
 * {@code isCritArrow()}）在 {@code hurt} 调用前后压 {@link EventCauseState}，
 * 事件构造器取即清。
 *
 * <p>嵌套伤害（荆棘反伤等）拿到的是 {@code false}：外层 {@code hurt} 里第一个构造出来的
 * 伤害事件已经把值取走了，与 paper 的语义一致。
 *
 * <p>两个构造器都要挂：运行时它们互不委托（`javap -c` 核对）。
 */
@Mixin(EntityDamageByEntityEvent.class)
public abstract class EntityDamageByEntityEventApiMixin {

    @Unique
    private boolean paperarc$critical;

    @Inject(method = "<init>(Lorg/bukkit/entity/Entity;Lorg/bukkit/entity/Entity;Lorg/bukkit/event/entity/EntityDamageEvent$DamageCause;D)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$captureCritical(Entity damager, Entity damagee, EntityDamageEvent.DamageCause cause,
                                          double damage, CallbackInfo ci) {
        this.paperarc$critical = EventCauseState.takeDamageCritical();
    }

    @Inject(method = "<init>(Lorg/bukkit/entity/Entity;Lorg/bukkit/entity/Entity;Lorg/bukkit/event/entity/EntityDamageEvent$DamageCause;Ljava/util/Map;Ljava/util/Map;)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$captureCriticalWithModifiers(
            Entity damager, Entity damagee, EntityDamageEvent.DamageCause cause,
            Map<EntityDamageEvent.DamageModifier, Double> modifiers,
            Map<EntityDamageEvent.DamageModifier, ? extends Function<? super Double, Double>> modifierFunctions,
            CallbackInfo ci) {
        this.paperarc$critical = EventCauseState.takeDamageCritical();
    }

    @Unique
    public boolean isCritical() {
        return this.paperarc$critical;
    }
}
