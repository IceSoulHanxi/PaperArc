package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Llama} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 *
 * <p><b>A4-5 真机纠正</b>：这里**不能**写 {@code extends com.destroystokyo.paper.entity.RangedEntity}。
 * Arclight 的 {@code EntityClassLookup.init} 会从每个 {@code EntityType} 的 Bukkit 类
 * 沿 {@code getInterfaces()} 向上收集所有「是 org.bukkit.entity.Entity 子类型」的接口，
 * 并要求每一个都有对应的 Craft 实现类；Mixin 会把 IfaceMixin **自身**也加进目标的接口表，
 * 于是 IfaceMixin 与 com.destroystokyo.paper.entity.RangedEntity 双双被判定为「没有实体类映射」，服务器直接崩在启动期。
 * 因此改为把父接口的方法**直接声明**在这里，不建立继承关系。</p>
 */
@Mixin(targets = "org.bukkit.entity.Llama", remap = false)
public interface LlamaIfaceMixin {

    @Unique
    public abstract boolean inCaravan();

    @Unique
    public abstract void joinCaravan(org.bukkit.entity.Llama p0);

    @Unique
    public abstract void leaveCaravan();

    @Unique
    public abstract org.bukkit.entity.Llama getCaravanHead();

    @Unique
    public abstract boolean hasCaravanTail();

    @Unique
    public abstract org.bukkit.entity.Llama getCaravanTail();
    @Unique
    public abstract void rangedAttack(org.bukkit.entity.LivingEntity target, float charge);

    @Unique
    public abstract void setChargingAttack(boolean raiseHands);

}
