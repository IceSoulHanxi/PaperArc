package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.inventory.CraftMetaColorableArmor;
import org.bukkit.craftbukkit.v.inventory.CraftMetaLeatherArmor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper 的 {@code LeatherArmorMeta#isDyed()}。
 *
 * <p>{@code LeatherArmorMeta} 在 CraftBukkit 里有**两个互不继承的实现类**：
 * {@code CraftMetaLeatherArmor} 与 {@code CraftMetaColorableArmor}（后者继承
 * {@code CraftMetaArmor}），只给前者加实现体的话，头盔/胸甲之类走后者的物品
 * 一调就 {@code AbstractMethodError}（真机实测）。所以这里是多目标 mixin。</p>
 */
// remap = false：多目标 mixin 的 @Shadow 不能是可重映射的（见 CraftMetaBookApiMixin 注释）
@Mixin(value = {CraftMetaLeatherArmor.class, CraftMetaColorableArmor.class}, remap = false)
public abstract class CraftMetaLeatherArmorApiMixin {

    @Shadow(remap = false)
    public abstract org.bukkit.Color getColor();

    @Unique
    public boolean isDyed() {
        // CraftMetaLeatherArmor.hasColor() 是包级私有，等价判据：颜色不是默认皮革色
        return !Bukkit.getItemFactory().getDefaultLeatherColor().equals(this.getColor());
    }
}
