package dev.paperarc.mixin.common.api;

import java.util.Date;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.base.Preconditions;
import org.bukkit.BanEntry;
import org.bukkit.craftbukkit.v.ban.CraftProfileBanList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds paper PlayerProfile addBan overload missing from Arclight CraftBukkit.
 * Paper ref: patches/server/Fix-BanList-API.patch (CraftProfileBanList).
 */
@Mixin(CraftProfileBanList.class)
public abstract class CraftProfileBanListApiMixin {

    @Shadow
    public abstract BanEntry<org.bukkit.profile.PlayerProfile> addBan(com.mojang.authlib.GameProfile profile, String reason, Date expires, String source);

    @Unique
    @SuppressWarnings("unchecked")
    public BanEntry<PlayerProfile> addBan(PlayerProfile target, String reason, Date expires, String source) {
        Preconditions.checkArgument(target != null, "Target cannot be null");
        // PlayerProfile#buildGameProfile() is a Paper addition missing from the
        // spigot-api compile classpath -> reflective access with a fallback
        // reconstruction from id+name.
        com.mojang.authlib.GameProfile gameProfile;
        try {
            java.lang.reflect.Method m = target.getClass().getMethod("buildGameProfile");
            gameProfile = (com.mojang.authlib.GameProfile) m.invoke(target);
        } catch (ReflectiveOperationException e) {
            gameProfile = new com.mojang.authlib.GameProfile(target.getUniqueId(), target.getName());
        }
        return (BanEntry<PlayerProfile>) (BanEntry<?>) this.addBan(gameProfile, reason, expires, source);
    }
}
