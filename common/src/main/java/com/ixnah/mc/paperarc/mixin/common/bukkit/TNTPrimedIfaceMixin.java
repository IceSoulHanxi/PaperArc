package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * A4-4：给 {@code org.bukkit.entity.TNTPrimed} 补上 paper-api 的 default 方法
 * （运行时接口里一个都没有，插件调用即 NoSuchMethodError）。
 */
@Mixin(targets = "org.bukkit.entity.TNTPrimed", remap = false)
public interface TNTPrimedIfaceMixin {

    @Unique
    public default org.bukkit.Location getSourceLoc() {
        return ((org.bukkit.entity.Entity) this).getOrigin();
    }
}
