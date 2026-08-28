package com.ixnah.mc.paperarc.mixin.common.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Date;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.base.Preconditions;
import org.bukkit.BanEntry;
import org.bukkit.craftbukkit.v1_20_R1.ban.CraftProfileBanList;
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

    /**
     * Paper-added {@code CraftPlayerProfile#buildGameProfile()}, resolved against
     * the runtime CraftBukkit class (not compile-visible); null when unavailable,
     * degrading addBan to reconstruction from id+name.
     */
    @Unique
    private static final MethodHandle PAPERARC$BUILD_GAME_PROFILE = paperarc$buildGameProfileHandle();

    @Unique
    private static MethodHandle paperarc$buildGameProfileHandle() {
        try {
            Class<?> cbProfile = Class.forName("org.bukkit.craftbukkit.v1_20_R1.profile.CraftPlayerProfile");
            return MethodHandles.privateLookupIn(cbProfile, MethodHandles.lookup())
                    .findVirtual(cbProfile, "buildGameProfile",
                            MethodType.methodType(com.mojang.authlib.GameProfile.class));
        } catch (ReflectiveOperationException e) {
            return null; // addBan() degrades to the reconstructed GameProfile
        }
    }

    @Unique
    @SuppressWarnings("unchecked")
    public BanEntry<PlayerProfile> addBan(PlayerProfile target, String reason, Date expires, String source) {
        Preconditions.checkArgument(target != null, "Target cannot be null");
        // PlayerProfile#buildGameProfile() is a Paper addition missing from the
        // spigot-api compile classpath -> MethodHandle access with a fallback
        // reconstruction from id+name.
        com.mojang.authlib.GameProfile gameProfile;
        if (PAPERARC$BUILD_GAME_PROFILE == null) {
            gameProfile = new com.mojang.authlib.GameProfile(target.getUniqueId(), target.getName());
        } else {
            try {
                gameProfile = (com.mojang.authlib.GameProfile) PAPERARC$BUILD_GAME_PROFILE.invoke(target);
            } catch (Throwable t) {
                gameProfile = new com.mojang.authlib.GameProfile(target.getUniqueId(), target.getName());
            }
        }
        return (BanEntry<PlayerProfile>) (BanEntry<?>) this.addBan(gameProfile, reason, expires, source);
    }
}
