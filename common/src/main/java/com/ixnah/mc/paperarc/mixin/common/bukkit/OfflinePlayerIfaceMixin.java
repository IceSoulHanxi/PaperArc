package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.OfflinePlayer} (generated, trimmed for 1.20.1).
 * Adds 3 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.OfflinePlayer", remap = false)
public interface OfflinePlayerIfaceMixin {

    @Unique
    public abstract boolean isConnected();

    @Unique
    public abstract long getLastLogin();

    @Unique
    public abstract long getLastSeen();
    // ===== A4-4：paper-api 的 default 方法体照搬（运行时接口里一个都没有）=====

    @Unique
    public default org.bukkit.BanEntry banPlayer(String reason) {
        return banPlayer(reason, null, null);
    }

    @Unique
    public default org.bukkit.BanEntry banPlayer(String reason, String source) {
        return banPlayer(reason, null, source);
    }

    @Unique
    public default org.bukkit.BanEntry banPlayer(String reason, java.util.Date expires) {
        return banPlayer(reason, expires, null);
    }

    @Unique
    public default org.bukkit.BanEntry banPlayer(String reason, java.util.Date expires, String source) {
        return banPlayer(reason, expires, source, true);
    }

    @Unique
    public default org.bukkit.BanEntry banPlayer(String reason, java.util.Date expires, String source, boolean kickIfOnline) {
        org.bukkit.OfflinePlayer self = (org.bukkit.OfflinePlayer) this;
        org.bukkit.BanEntry banEntry = org.bukkit.Bukkit.getServer()
                .getBanList(org.bukkit.BanList.Type.NAME)
                .addBan(self.getName(), reason, expires, source);
        if (kickIfOnline && self.isOnline()) {
            self.getPlayer().kickPlayer(reason);
        }
        return banEntry;
    }

}
