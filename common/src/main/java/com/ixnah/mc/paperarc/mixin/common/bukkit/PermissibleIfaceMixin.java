package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import java.util.Set;
import net.kyori.adventure.util.TriState;
import org.bukkit.plugin.Plugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-3：{@link org.bukkit.permissions.Permissible} 的 paper default 方法体（照抄 paper-api）。
 */
@Mixin(targets = "org.bukkit.permissions.Permissible", remap = false)
public interface PermissibleIfaceMixin {

    @Unique
    public default TriState permissionValue(Permission permission) {
        Permissible self = (Permissible) this;
        return self.isPermissionSet(permission) ? TriState.byBoolean(self.hasPermission(permission)) : TriState.NOT_SET;
    }

    @Unique
    public default TriState permissionValue(String permission) {
        Permissible self = (Permissible) this;
        return self.isPermissionSet(permission) ? TriState.byBoolean(self.hasPermission(permission)) : TriState.NOT_SET;
    }
}
