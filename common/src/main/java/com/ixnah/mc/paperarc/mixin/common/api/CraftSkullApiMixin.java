package com.ixnah.mc.paperarc.mixin.common.api;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.world.item.component.ResolvableProfile;
import org.bukkit.craftbukkit.v.block.CraftSkull;
import org.bukkit.craftbukkit.v.profile.CraftPlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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
    private ResolvableProfile profile;

    @Unique
    public void setPlayerProfile(com.destroystokyo.paper.profile.PlayerProfile profile) {
        Preconditions.checkNotNull(profile, "profile");
        // paper-api here exposes neither SharedPlayerProfile nor
        // buildGameProfile(); convert via the basic accessors instead.
        GameProfile gameProfile = new GameProfile(profile.getUniqueId(), profile.getName());
        for (ProfileProperty property : profile.getProperties()) {
            gameProfile.getProperties().put(property.getName(), new Property(property.getName(), property.getValue(), property.getSignature()));
        }
        this.profile = CraftPlayerProfile.validateSkullProfile(new ResolvableProfile(gameProfile));
    }

    @Unique
    public com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile() {
        ResolvableProfile resolvable = this.profile;
        return resolvable == null ? null : CraftSkullApiMixin.paperarc$paperProfile(resolvable.gameProfile());
    }

    @Unique
    private static com.destroystokyo.paper.profile.PlayerProfile paperarc$paperProfile(GameProfile gameProfile) {
        com.ixnah.mc.paperarc.bridge.PaperarcProfileHandler handler =
            new com.ixnah.mc.paperarc.bridge.PaperarcProfileHandler(gameProfile);
        com.destroystokyo.paper.profile.PlayerProfile proxy =
            (com.destroystokyo.paper.profile.PlayerProfile) Proxy.newProxyInstance(
                CraftSkullApiMixin.class.getClassLoader(),
                new Class<?>[] {com.destroystokyo.paper.profile.PlayerProfile.class},
                handler
            );
        handler.setSelf(proxy);
        return proxy;
    }

    // PaperarcProfileHandler 在 bridge/：mixin 包内的类被合并后的 CraftSkull 字节码引用
    // 即 IllegalClassLoadError，内嵌类还会让 InnerClasses 属性与目标类互相矛盾。
}
