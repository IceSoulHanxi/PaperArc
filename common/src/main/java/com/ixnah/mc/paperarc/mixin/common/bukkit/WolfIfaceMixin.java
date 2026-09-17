package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/**
 * A4-3 父接口差集：给 {@code org.bukkit.entity.Wolf} 补上 paper 声明的父接口
 * {@code io.papermc.paper.entity.CollarColorable}。父接口的两个方法运行时本来就有，只补继承关系。
 *
 * <p><b>A4-5 真机纠正</b>：这里**不能**写 {@code extends io.papermc.paper.entity.CollarColorable}。
 * Arclight 的 {@code EntityClassLookup.init} 会从每个 {@code EntityType} 的 Bukkit 类
 * 沿 {@code getInterfaces()} 向上收集所有「是 org.bukkit.entity.Entity 子类型」的接口，
 * 并要求每一个都有对应的 Craft 实现类；Mixin 会把 IfaceMixin **自身**也加进目标的接口表，
 * 于是 IfaceMixin 与 io.papermc.paper.entity.CollarColorable 双双被判定为「没有实体类映射」，服务器直接崩在启动期。
 * 因此改为把父接口的方法**直接声明**在这里，不建立继承关系。</p>
 */
@Mixin(targets = "org.bukkit.entity.Wolf", remap = false)
public interface WolfIfaceMixin {
}
