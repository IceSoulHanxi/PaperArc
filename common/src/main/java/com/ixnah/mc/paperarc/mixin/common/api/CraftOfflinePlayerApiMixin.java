package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.CraftPlayerProfile;
import org.bukkit.craftbukkit.v.CraftOfflinePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper 的 {@code OfflinePlayer#getPlayerProfile()}。Arclight 只有 Bukkit 的
 * {@code getPlayerProfile()} 返回 {@code org.bukkit.profile.PlayerProfile}（不同类型），
 * 这里按 destroystokyo 的类型重新包一个。
 */
@Mixin(CraftOfflinePlayer.class)
public abstract class CraftOfflinePlayerApiMixin {

    @Unique
    public com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile() {
        org.bukkit.OfflinePlayer self = (org.bukkit.OfflinePlayer) (Object) this;
        // 从没登录过的 UUID 名字为 null；CraftPlayerProfile 自持可空的 id/name
        return new CraftPlayerProfile(self.getUniqueId(), self.getName());
    }

    @Unique
    public boolean isConnected() {
        return ((org.bukkit.OfflinePlayer) (Object) this).isOnline();
    }

    @Unique
    public long getLastLogin() {
        // Arclight 没有 Paper 的 lastLogin/lastSeen 统计，能拿到的只有 CraftBukkit 的
        // getLastPlayed()（最后一次退出的时间戳）；在线时 lastSeen 按"现在"给。
        return ((org.bukkit.OfflinePlayer) (Object) this).getLastPlayed();
    }

    @Unique
    public long getLastSeen() {
        org.bukkit.OfflinePlayer self = (org.bukkit.OfflinePlayer) (Object) this;
        return self.isOnline() ? System.currentTimeMillis() : self.getLastPlayed();
    }
}
