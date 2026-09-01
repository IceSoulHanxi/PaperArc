package com.ixnah.mc.paperarc.mixin.common.api;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.common.base.Preconditions;
import com.ixnah.mc.paperarc.bridge.PaperarcProfileHandler;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.craftbukkit.v.block.CraftSkull;
import org.bukkit.craftbukkit.v.profile.CraftPlayerProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Proxy;

/**
 * Port of Paper's Add-setPlayerProfile-API-for-Skulls.patch additions on
 * {@link CraftSkull}: {@code Skull#setPlayerProfile(PlayerProfile)} and
 * {@code Skull#getPlayerProfile()}.
 *
 * <p>Arclight ships no {@code com.destroystokyo.paper.profile} implementation
 * class (paper-server's {@code CraftPlayerProfile} is absent), so
 * {@code getPlayerProfile} returns a dynamic-proxy adapter over the vanilla
 * {@link GameProfile}, reusing CraftBukkit's
 * {@code org.bukkit.craftbukkit.v.profile.CraftPlayerProfile} for the
 * bukkit-side contract (textures, completeness). Profile completion/update
 * methods degrade to offline no-network behaviour (sync-fallback).
 */
@Mixin(CraftSkull.class)
public abstract class CraftSkullApiMixin {

    @Shadow
    private GameProfile profile;

    @Unique
    public void setPlayerProfile(com.destroystokyo.paper.profile.PlayerProfile profile) {
        Preconditions.checkNotNull(profile, "profile");
        // 1.20.1 paper-api has no buildGameProfile(); convert via the basic accessors.
        GameProfile gameProfile = new GameProfile(profile.getId(), profile.getName());
        for (ProfileProperty property : profile.getProperties()) {
            gameProfile.getProperties().put(property.getName(), new Property(property.getName(), property.getValue(), property.getSignature()));
        }
        this.profile = CraftPlayerProfile.validateSkullProfile(gameProfile);
    }

    @Unique
    public com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile() {
        GameProfile gameProfile = this.profile;
        return gameProfile == null ? null : CraftSkullApiMixin.paperarc$paperProfile(gameProfile);
    }

    @Unique
    private static com.destroystokyo.paper.profile.PlayerProfile paperarc$paperProfile(GameProfile gameProfile) {
        PaperarcProfileHandler handler = new PaperarcProfileHandler(gameProfile);
        com.destroystokyo.paper.profile.PlayerProfile proxy =
            (com.destroystokyo.paper.profile.PlayerProfile) Proxy.newProxyInstance(
                CraftSkullApiMixin.class.getClassLoader(),
                new Class<?>[] {com.destroystokyo.paper.profile.PlayerProfile.class},
                handler
            );
            handler.setSelf(proxy);
            return proxy;
    }
}
