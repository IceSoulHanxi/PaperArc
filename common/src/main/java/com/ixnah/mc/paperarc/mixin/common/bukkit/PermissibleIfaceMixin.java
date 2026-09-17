package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * A4-4：给 {@code org.bukkit.permissions.Permissible} 补上 paper-api 的 default 方法
 * （运行时接口里一个都没有，插件调用即 NoSuchMethodError）。
 */
@Mixin(targets = "org.bukkit.permissions.Permissible", remap = false)
public interface PermissibleIfaceMixin {

    @Unique
    public default net.kyori.adventure.util.TriState permissionValue(String permission) {
        org.bukkit.permissions.Permissible self = (org.bukkit.permissions.Permissible) this;
        return self.isPermissionSet(permission)
                ? net.kyori.adventure.util.TriState.byBoolean(self.hasPermission(permission))
                : net.kyori.adventure.util.TriState.NOT_SET;
    }

    @Unique
    public default net.kyori.adventure.util.TriState permissionValue(org.bukkit.permissions.Permission permission) {
        org.bukkit.permissions.Permissible self = (org.bukkit.permissions.Permissible) this;
        return self.isPermissionSet(permission)
                ? net.kyori.adventure.util.TriState.byBoolean(self.hasPermission(permission))
                : net.kyori.adventure.util.TriState.NOT_SET;
    }
}
