package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/**
 * A5-1 父接口差集：给 {@code org.bukkit.entity.Salmon} 补上 paper 声明的父接口
 * {@code io.papermc.paper.entity.SchoolableFish}。终端方法实现在
 * {@code CraftSchoolableFishApiMixin} 上（CraftCod/CraftSalmon/CraftTropicalFish 互不继承）。
 *
 * <p>能安全 {@code extends} 是因为 {@code mixin/common/server/EntityClassLookupMixin}
 * 已让 Arclight 的实体类型自检放行非 {@code org.bukkit.entity.} 的能力接口。</p>
 */
@Mixin(targets = "org.bukkit.entity.Salmon", remap = false)
public interface SalmonIfaceMixin extends io.papermc.paper.entity.SchoolableFish {
}
