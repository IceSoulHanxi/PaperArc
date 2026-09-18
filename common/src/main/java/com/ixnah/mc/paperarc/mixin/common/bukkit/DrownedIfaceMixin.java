package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/**
 * A4-3 父接口差集：给 {@code org.bukkit.entity.Drowned} 补上 paper 声明的父接口
 * {@code com.destroystokyo.paper.entity.RangedEntity}。终端方法 rangedAttack/setChargingAttack 统一实现在 CraftMobApiMixin 上。
 *
 * <p><b>A5-1</b>：A4 曾因 Arclight 的实体类型自检（{@code EntityClassLookup.init} 要求每个
 * {@code org.bukkit.entity.Entity} 子接口都有 Craft 映射）而把这里的 {@code extends} 拆成直接声明；
 * 现已由 {@code mixin/common/server/EntityClassLookupMixin} 按包名放行非 {@code org.bukkit.entity.}
 * 的能力接口，继承关系恢复，插件的 {@code instanceof} 也随之成立。</p>
 */
@Mixin(targets = "org.bukkit.entity.Drowned", remap = false)
public interface DrownedIfaceMixin extends com.destroystokyo.paper.entity.RangedEntity {
}
