package com.ixnah.mc.paperarc.bridge;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.bukkit.craftbukkit.v.profile.CraftPlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Adapts a vanilla {@link GameProfile} to paper-api's
 * {@code com.destroystokyo.paper.profile.PlayerProfile}. Bukkit-side methods
 * (textures, serialize, buildResolvableProfile) delegate to CraftBukkit's
 * spigot-side {@code CraftPlayerProfile}; paper-side property and completion
 * methods are implemented locally without network access.
 *
 * <p>Lives in {@code bridge} (not the mixin package) so that the merged
 * {@code CraftSkull} bytecode may reference it directly — Mixin forbids
 * referencing classes inside a defined mixin package.</p>
 */
public final class PaperarcProfileHandler implements InvocationHandler {

    private GameProfile profile;
    private CraftPlayerProfile cbProfile;
    private PlayerProfile self;

    public PaperarcProfileHandler(GameProfile profile) {
        this.profile = profile;
        this.cbProfile = new CraftPlayerProfile(profile);
    }

    /** Called by the skull mixin right after the proxy is created. */
    public void setSelf(PlayerProfile self) {
        this.self = self;
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
            result.add(new ProfileProperty(entry.getKey(), entry.getValue().getValue(), entry.getValue().getSignature()));
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
