package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.inventory.meta.Damageable}
 * （与 {@link DamageableIfaceMixin} 的 {@code org.bukkit.entity.Damageable} 只是同名，
 * 是两个完全不同的接口 —— 生成脚本按简单名命名文件，会撞车，这里手工拆开）。
 * 实现体在 {@code api.CraftMetaItemPaperApiMixin}。
 */
@Mixin(targets = "org.bukkit.inventory.meta.Damageable", remap = false)
public interface ItemMetaDamageableIfaceMixin {

    @Unique
    public abstract boolean hasDamageValue();

    @Unique
    public abstract void resetDamage();
}
