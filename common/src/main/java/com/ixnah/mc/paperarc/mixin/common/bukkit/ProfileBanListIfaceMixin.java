package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.ban.ProfileBanList} (generated).
 * Adds 6 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.ban.ProfileBanList", remap = false)
public interface ProfileBanListIfaceMixin {

    @Unique
    public abstract <E extends org.bukkit.BanEntry<? super com.destroystokyo.paper.profile.PlayerProfile>> E addBan(org.bukkit.profile.PlayerProfile p0, java.lang.String p1, java.time.Duration p2, java.lang.String p3);

    @Unique
    public abstract <E extends org.bukkit.BanEntry<? super com.destroystokyo.paper.profile.PlayerProfile>> E addBan(org.bukkit.profile.PlayerProfile p0, java.lang.String p1, java.time.Instant p2, java.lang.String p3);

    @Unique
    public abstract <E extends org.bukkit.BanEntry<? super com.destroystokyo.paper.profile.PlayerProfile>> E getBanEntry(org.bukkit.profile.PlayerProfile p0);

    @Unique
    public abstract boolean isBanned(org.bukkit.profile.PlayerProfile p0);

    @Unique
    public abstract org.bukkit.BanEntry<com.destroystokyo.paper.profile.PlayerProfile> addBan(com.destroystokyo.paper.profile.PlayerProfile p0, java.lang.String p1, java.util.Date p2, java.lang.String p3);

    @Unique
    public abstract void pardon(org.bukkit.profile.PlayerProfile p0);

}