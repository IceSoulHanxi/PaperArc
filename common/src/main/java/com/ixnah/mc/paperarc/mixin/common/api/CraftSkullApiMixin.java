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
        PaperarcProfileHandler handler = new PaperarcProfileHandler(gameProfile);
        com.destroystokyo.paper.profile.PlayerProfile proxy =
            (com.destroystokyo.paper.profile.PlayerProfile) Proxy.newProxyInstance(
                CraftSkullApiMixin.class.getClassLoader(),
                new Class<?>[] {com.destroystokyo.paper.profile.PlayerProfile.class},
                handler
            );
        handler.self = proxy;
        return proxy;
    }

    /**
     * Adapts a vanilla {@link GameProfile} to paper-api's
     * {@code com.destroystokyo.paper.profile.PlayerProfile}. Bukkit-side
     * methods (textures, serialize, buildResolvableProfile) delegate to
     * CraftBukkit's spigot-side CraftPlayerProfile; paper-side property and
     * completion methods are implemented locally without network access.
     */
    @Unique
    private static final class PaperarcProfileHandler implements InvocationHandler {

        private GameProfile profile;
        private CraftPlayerProfile cbProfile;
        private com.destroystokyo.paper.profile.PlayerProfile self;

        private PaperarcProfileHandler(GameProfile profile) {
            this.profile = profile;
            this.cbProfile = new CraftPlayerProfile(profile);
        }

        private void paperarc$rebuild(java.util.UUID id, String name) {
            GameProfile rebuilt = new GameProfile(
                id != null ? id : this.profile.getId(),
                name != null ? name : this.profile.getName()
            );
            rebuilt.getProperties().putAll(this.profile.getProperties());
            this.profile = rebuilt;
            this.cbProfile = new CraftPlayerProfile(this.profile);
        }

        private static Set<ProfileProperty> paperarc$toPaperProperties(PropertyMap properties) {
            Set<ProfileProperty> result = new HashSet<>();
            for (Map.Entry<String, Property> entry : properties.entries()) {
                result.add(new ProfileProperty(entry.getKey(), entry.getValue().value(), entry.getValue().signature()));
            }
            return result;
        }

        private boolean paperarc$isComplete() {
            return this.profile.getId() != null && this.profile.getName() != null;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "getName":
                    return this.profile.getName();
                case "setName":
                    String name = (String) args[0];
                    this.paperarc$rebuild(null, name);
                    return name;
                case "getId":
                case "getUniqueId":
                    return this.profile.getId();
                case "setId":
                    java.util.UUID id = (java.util.UUID) args[0];
                    this.paperarc$rebuild(id, null);
                    return id;
                case "getTextures":
                    return this.cbProfile.getTextures();
                case "setTextures":
                    this.cbProfile.setTextures((PlayerTextures) args[0]);
                    return null;
                case "getProperties":
                    return paperarc$toPaperProperties(this.profile.getProperties());
                case "hasProperty":
                    return this.profile.getProperties().containsKey((String) args[0]);
                case "setProperty":
                    ProfileProperty property = (ProfileProperty) args[0];
                    this.profile.getProperties().put(property.getName(), new Property(property.getName(), property.getValue(), property.getSignature()));
                    return null;
                case "setProperties":
                    for (ProfileProperty pp : (Collection<ProfileProperty>) args[0]) {
                        this.profile.getProperties().put(pp.getName(), new Property(pp.getName(), pp.getValue(), pp.getSignature()));
                    }
                    return null;
                case "removeProperty":
                    if (args[0] instanceof String) {
                        return !this.profile.getProperties().removeAll((String) args[0]).isEmpty();
                    }
                    return !this.profile.getProperties().removeAll(((ProfileProperty) args[0]).getName()).isEmpty();
                case "removeProperties":
                    boolean removed = false;
                    for (ProfileProperty pp : (Collection<ProfileProperty>) args[0]) {
                        removed |= !this.profile.getProperties().removeAll(pp.getName()).isEmpty();
                    }
                    return removed;
                case "clearProperties":
                    this.profile.getProperties().clear();
                    return null;
                case "hasTextures":
                    return this.profile.getProperties().containsKey("textures");
                case "isComplete":
                    return this.paperarc$isComplete();
                case "completeFromCache":
                case "complete":
                    // No offline-profile lookup infrastructure in Arclight:
                    // degrade to the current resolution state (no network).
                    return this.paperarc$isComplete();
                case "update":
                    // sync-fallback: no Mojang API refresh infrastructure
                    return CompletableFuture.completedFuture(this.self);
                case "buildGameProfile":
                    return this.profile;
                case "buildResolvableProfile":
                    return new ResolvableProfile(this.profile);
                case "serialize":
                    return this.cbProfile.serialize();
                case "toString":
                    return this.profile.toString();
                case "hashCode":
                    return this.profile.hashCode();
                case "equals":
                    Object other = args[0];
                    if (other instanceof Proxy) {
                        return Proxy.getInvocationHandler(other) == this;
                    }
                    return this.profile.equals(other);
                default:
                    throw new UnsupportedOperationException("PlayerProfile." + method.getName());
            }
        }
    }
}
