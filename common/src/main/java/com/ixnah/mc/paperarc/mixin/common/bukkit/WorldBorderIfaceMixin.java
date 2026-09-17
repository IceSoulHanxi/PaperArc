package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * A4-4：给 {@code org.bukkit.WorldBorder} 补上 paper-api 的 default 方法
 * （运行时接口里一个都没有，插件调用即 NoSuchMethodError）。
 */
@Mixin(targets = "org.bukkit.WorldBorder", remap = false)
public interface WorldBorderIfaceMixin {

    @Unique
    public default boolean isInBounds(org.bukkit.Location location) {
        org.bukkit.WorldBorder self = (org.bukkit.WorldBorder) this;
        org.bukkit.Location center = self.getCenter();
        double radius = self.getSize() / 2.0D;
        return location.getWorld().equals(center.getWorld())
                && Math.abs(location.getX() - center.getX()) <= radius
                && Math.abs(location.getZ() - center.getZ()) <= radius;
    }
}
