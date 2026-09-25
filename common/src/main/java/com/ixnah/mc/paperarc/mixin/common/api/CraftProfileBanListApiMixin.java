package com.ixnah.mc.paperarc.mixin.common.api;

import java.util.Date;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.base.Preconditions;
import net.minecraft.server.players.NameAndId;
import org.bukkit.BanEntry;
import org.bukkit.craftbukkit.v.ban.CraftProfileBanList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds paper PlayerProfile addBan overload missing from Arclight CraftBukkit.
 * Paper ref: patches/server/Fix-BanList-API.patch (CraftProfileBanList).
 *
 * <p>1.21.11：CB 的封禁表改以 {@code NameAndId} 为键（原来是 {@code GameProfile}），
 * 且 {@code org.bukkit.profile.PlayerProfile} 的 Duration/Instant/getBanEntry/isBanned/pardon
 * 重载 CB 已自带（B2-4 补的那一组若保留会被 Mixin 以 {@code Discarding @Unique} 丢弃），这里只剩 paper 自有的
 * {@code com.destroystokyo.paper.profile.PlayerProfile} 重载。封禁只看 id + name，不需要完整 GameProfile。
 */
@Mixin(CraftProfileBanList.class)
public abstract class CraftProfileBanListApiMixin {

    @Shadow
    public abstract BanEntry<org.bukkit.profile.PlayerProfile> addBan(NameAndId profile, String reason, Date expires, String source);

    @Unique
    @SuppressWarnings("unchecked")
    public BanEntry<PlayerProfile> addBan(PlayerProfile target, String reason, Date expires, String source) {
        Preconditions.checkArgument(target != null, "Target cannot be null");
        return (BanEntry<PlayerProfile>) (BanEntry<?>) this.addBan(
                new NameAndId(target.getId() == null ? new java.util.UUID(0L, 0L) : target.getId(),
                        target.getName() == null ? "" : target.getName()),
                reason, expires, source);
    }
}
