package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Function;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * paper 的 {@code EntityDamageByBlockEvent#getDamagerBlockState()}。
 *
 * <p>paper 把它做成构造器形参（CraftEventFactory 在方块变化之前先取快照）。运行时的两个
 * 构造器都没有这个形参，但事件就在伤害发生的那一刻构造，此时 {@code getDamager()}
 * 指向的仍是伤害源方块本身，所以在构造器里 {@code getState()} 取快照与 paper 等价
 * （不能放到 getter 里现取：方块可能已经变了）。
 */
@Mixin(EntityDamageByBlockEvent.class)
public abstract class EntityDamageByBlockEventApiMixin {

    @Unique
    private BlockState paperarc$damagerBlockState;

    @Inject(method = "<init>(Lorg/bukkit/block/Block;Lorg/bukkit/entity/Entity;Lorg/bukkit/event/entity/EntityDamageEvent$DamageCause;D)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$snapshotDamagerState(Block damager, Entity damagee,
                                               EntityDamageEvent.DamageCause cause, double damage,
                                               CallbackInfo ci) {
        this.paperarc$damagerBlockState = damager == null ? null : damager.getState();
    }

    @Inject(method = "<init>(Lorg/bukkit/block/Block;Lorg/bukkit/entity/Entity;Lorg/bukkit/event/entity/EntityDamageEvent$DamageCause;Ljava/util/Map;Ljava/util/Map;)V",
            at = @At("RETURN"), remap = false)
    private void paperarc$snapshotDamagerStateModifiers(
            Block damager, Entity damagee, EntityDamageEvent.DamageCause cause,
            Map<EntityDamageEvent.DamageModifier, Double> modifiers,
            Map<EntityDamageEvent.DamageModifier, ? extends Function<? super Double, Double>> modifierFunctions,
            CallbackInfo ci) {
        this.paperarc$damagerBlockState = damager == null ? null : damager.getState();
    }

    @Unique
    public BlockState getDamagerBlockState() {
        return this.paperarc$damagerBlockState;
    }
}
