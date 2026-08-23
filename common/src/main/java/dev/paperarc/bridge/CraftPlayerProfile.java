package dev.paperarc.bridge;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.profile.PlayerTextures;

/**
 * Minimal paper-api {@link com.destroystokyo.paper.profile.PlayerProfile}
 * implementation wrapping a mutable authlib {@link GameProfile} (blocked
 * batch 2 infrastructure; replaces the dynamic-proxy adapter previously
 * needed for {@code Skull#getPlayerProfile}).
 *
 * <p>Textures round-trip through the packed base64 {@code textures} property
 * exactly like Mojang ships them; locally built payloads (via
 * {@link #setTextures}) are unsigned. Completion resolves through the
 * server {@code GameProfileCache}; {@code complete(...)} additionally fills
 * properties from the {@code MinecraftSessionService}. {@link #update()} is
 * a sync-fallback: the fetch runs synchronously on the calling thread and
 * the future completes immediately.</p>
 */
public class CraftPlayerProfile implements com.destroystokyo.paper.profile.PlayerProfile {

    private static final String TEXTURES_PROPERTY = "textures";

    private GameProfile profile;

    public CraftPlayerProfile(GameProfile gameProfile) {
        this.profile = Preconditions.checkNotNull(gameProfile, "gameProfile");
    }

    /** Mirrors the given GameProfile (shares its property map, Paper semantics). */
    public static CraftPlayerProfile asBukkitMirror(GameProfile gameProfile) {
        return new CraftPlayerProfile(gameProfile);
    }

    /**
     * Universal conversion: any paper-api {@code PlayerProfile} implementation
     * becomes a fresh authlib {@link GameProfile} (deep property copy).
     */
    public static GameProfile asAuthlibCopy(com.destroystokyo.paper.profile.PlayerProfile profile) {
        Preconditions.checkNotNull(profile, "profile");
        GameProfile out = new GameProfile(profile.getId(), profile.getName());
        PropertyMap props = out.getProperties();
        for (ProfileProperty property : profile.getProperties()) {
            props.put(property.getName(),
                    new Property(property.getName(), property.getValue(), property.getSignature()));
        }
        return out;
    }

    /** The wrapped authlib profile (same accessor Paper's CraftPlayerProfile exposes). */
    public GameProfile getGameProfile() {
        return this.profile;
    }

    private void paperarc$rebuild(UUID id, String name) {
        GameProfile rebuilt = new GameProfile(
                id != null ? id : this.profile.getId(),
                name != null ? name : this.profile.getName());
        rebuilt.getProperties().putAll(this.profile.getProperties());
        this.profile = rebuilt;
    }

    // ===== identity =====

    @Override
    public UUID getId() {
        return this.profile.getId();
    }

    @Override
    public UUID setId(UUID uniqueId) {
        paperarc$rebuild(uniqueId, null);
        return uniqueId;
    }

    @Override
    public String getName() {
        return this.profile.getName();
    }

    @Override
    public String setName(String name) {
        paperarc$rebuild(null, name);
        return name;
    }

    @Override
    public UUID getUniqueId() {
        return this.profile.getId();
    }

    // ===== properties =====

    @Override
    public Set<ProfileProperty> getProperties() {
        Set<ProfileProperty> out = new HashSet<>();
        for (Map.Entry<String, Property> entry : this.profile.getProperties().entries()) {
            Property property = entry.getValue();
            out.add(new ProfileProperty(entry.getKey(), property.value(), property.signature()));
        }
        return out;
    }

    @Override
    public boolean hasProperty(String name) {
        Preconditions.checkNotNull(name, "name");
        return this.profile.getProperties().containsKey(name);
    }

    @Override
    public void setProperty(ProfileProperty property) {
        Preconditions.checkNotNull(property, "property");
        this.profile.getProperties().put(property.getName(),
                new Property(property.getName(), property.getValue(), property.getSignature()));
    }

    @Override
    public void setProperties(Collection<ProfileProperty> properties) {
        Preconditions.checkNotNull(properties, "properties");
        PropertyMap map = this.profile.getProperties();
        for (ProfileProperty property : properties) {
            map.put(property.getName(),
                    new Property(property.getName(), property.getValue(), property.getSignature()));
        }
    }

    @Override
    public boolean removeProperty(String name) {
        Preconditions.checkNotNull(name, "name");
        return !this.profile.getProperties().removeAll(name).isEmpty();
    }

    @Override
    public void clearProperties() {
        this.profile.getProperties().clear();
    }

    @Override
    public boolean hasTextures() {
        return hasProperty(TEXTURES_PROPERTY);
    }

    // ===== completion =====

    /** authlib 6 GameProfile carries no isComplete(); compare fields instead. */
    @Override
    public boolean isComplete() {
        return this.profile.getId() != null && this.profile.getName() != null;
    }

    @Override
    public boolean completeFromCache() {
        return completeFromCache(true, true);
    }

    @Override
    public boolean completeFromCache(boolean lookupName) {
        return completeFromCache(lookupName, true);
    }

    /**
     * Resolves the missing half (id/name) from the server's in-memory
     * {@code GameProfileCache} — no network. {@code lookupName}: resolve the
     * name from the id; {@code lookupId}: resolve the id from the name.
     */
    @Override
    public boolean completeFromCache(boolean lookupName, boolean lookupId) {
        net.minecraft.server.MinecraftServer nms = paperarc$nmsServer();
        if (nms == null) {
            return isComplete();
        }
        if (getId() == null && getName() != null && lookupId) {
            nms.getProfileCache().get(getName()).ifPresent(found ->
                    paperarc$rebuild(found.getId(), null));
        } else if (getName() == null && getId() != null && lookupName) {
            nms.getProfileCache().get(getId()).ifPresent(found ->
                    paperarc$rebuild(null, found.getName()));
        }
        return isComplete();
    }

/**
     * Cache pass first, then a best-effort property fill from the session
     * service ({@code secure} toggles requireSecure on the authlib fetch).
     * Paper-parity deviation: the network fill runs on the calling thread.
     */
    @Override
    public boolean complete(boolean secure) {
        completeFromCache(true, true);
        paperarc$fillFromSessionService(secure);
        return isComplete();
    }

    /** Cache-only completion gated by the two lookup flags (no network). */
    @Override
    public boolean complete(boolean lookupName, boolean lookupId) {
        completeFromCache(lookupName, lookupId);
        return isComplete();
    }

    /**
     * Best-effort property fill from {@code MinecraftSessionService#fetchProfile};
     * swallows every failure (offline mode, no network) and leaves the profile
     * untouched.
     */
    private boolean paperarc$fillFromSessionService(boolean requireSecure) {
        net.minecraft.server.MinecraftServer nms = paperarc$nmsServer();
        if (nms == null || getId() == null) {
            return false;
        }
        try {
            com.mojang.authlib.yggdrasil.ProfileResult result =
                    nms.getSessionService().fetchProfile(getId(), requireSecure);
            if (result == null) {
                return false;
            }
            // authlib 6.x ProfileResult#profile() returns the GameProfile directly
            // (not an Optional).
            GameProfile found = result.profile();
            if (found == null || found.getId() == null) {
                return false;
            }
            if (getName() == null && found.getName() != null) {
                paperarc$rebuild(null, found.getName());
            }
            this.profile.getProperties().putAll(found.getProperties());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static net.minecraft.server.MinecraftServer paperarc$nmsServer() {
        if (!(Bukkit.getServer() instanceof CraftServer craftServer)) {
            return null;
        }
        return craftServer.getServer();
    }

    /**
     * Sync-fallback: performs the session-service fetch synchronously on the
     * calling thread (no worker pool) and completes immediately.
     */
    @Override
    public CompletableFuture<PlayerProfile> update() {
        paperarc$fillFromSessionService(true);
        return CompletableFuture.completedFuture(this);
    }

    // ===== serialization / cloning =====

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> out = new LinkedHashMap<>();
        if (getId() != null) {
            out.put("uniqueId", getId().toString());
        }
        if (getName() != null) {
            out.put("name", getName());
        }
        List<Map<String, Object>> properties = new ArrayList<>();
        for (ProfileProperty property : getProperties()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", property.getName());
            entry.put("value", property.getValue());
            if (property.getSignature() != null) {
                entry.put("signature", property.getSignature());
            }
            properties.add(entry);
        }
        out.put("properties", properties);
        return out;
    }

    @Override
    public CraftPlayerProfile clone() {
        GameProfile copy = new GameProfile(this.profile.getId(), this.profile.getName());
        copy.getProperties().putAll(this.profile.getProperties());
        return new CraftPlayerProfile(copy);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof com.destroystokyo.paper.profile.PlayerProfile other)) {
            return false;
        }
        return Objects.equals(getId(), other.getId())
                && Objects.equals(getName(), other.getName())
                && getProperties().equals(other.getProperties());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }

    @Override
    public String toString() {
        return "CraftArcPlayerProfile{id=" + getId() + ", name=" + getName()
                + ", properties=" + getProperties() + "}";
    }

    // ===== textures =====

    @Override
    public PlayerTextures getTextures() {
        return new PaperarcTextureView();
    }

    /** Copies the given texture state into a freshly packed (unsigned) textures property. */
    @Override
    public void setTextures(PlayerTextures textures) {
        Preconditions.checkNotNull(textures, "textures");
        PropertyMap props = this.profile.getProperties();
        props.removeAll(TEXTURES_PROPERTY);
        if (textures.isEmpty()) {
            return;
        }
        try {
            props.put(TEXTURES_PROPERTY, paperarc$packTextures(textures));
        } catch (Exception ignored) {
            // malformed URLs etc.: leave the profile texture-less
        }
    }

    private static Property paperarc$packTextures(PlayerTextures textures) throws Exception {
        JsonObject root = new JsonObject();
        root.addProperty("timestamp",
                textures.getTimestamp() > 0 ? textures.getTimestamp() : System.currentTimeMillis());
        root.addProperty("isPublic", true);
        JsonObject payload = new JsonObject();
        if (textures.getSkin() != null) {
            JsonObject skin = new JsonObject();
            skin.addProperty("url", textures.getSkin().toString());
            if (textures.getSkinModel() == PlayerTextures.SkinModel.SLIM) {
                JsonObject metadata = new JsonObject();
                metadata.addProperty("model", "slim");
                skin.add("metadata", metadata);
            }
            payload.add("SKIN", skin);
        }
        if (textures.getCape() != null) {
            JsonObject cape = new JsonObject();
            cape.addProperty("url", textures.getCape().toString());
            payload.add("CAPE", cape);
        }
        root.add("textures", payload);
        String packed = Base64.getEncoder()
                .encodeToString(root.toString().getBytes(StandardCharsets.UTF_8));
        return new Property(TEXTURES_PROPERTY, packed);
    }

    /**
     * Lazy view over the packed {@code textures} property: decodes the base64
     * JSON payload on demand; setters rewrite the property through the owning
     * profile (unsigned re-packing).
     */
    private final class PaperarcTextureView implements PlayerTextures {

        private JsonObject paperarc$decodeRoot() {
            Collection<Property> stored =
                    CraftPlayerProfile.this.profile.getProperties().get(TEXTURES_PROPERTY);
            if (stored == null || stored.isEmpty()) {
                return null;
            }
            try {
                String raw = new String(
                        Base64.getDecoder().decode(stored.iterator().next().value()),
                        StandardCharsets.UTF_8);
                return JsonParser.parseString(raw).getAsJsonObject();
            } catch (Exception e) {
                return null;
            }
        }

        private JsonObject paperarc$texturesOf(JsonObject root) {
            if (root == null || !root.has("textures") || !root.get("textures").isJsonObject()) {
                return null;
            }
            return root.getAsJsonObject("textures");
        }

        private URL paperarc$url(JsonObject section) {
            if (section == null || !section.has("url")) {
                return null;
            }
            try {
                return URI.create(section.get("url").getAsString()).toURL();
            } catch (Exception e) {
                return null;
            }
        }

        /** Decodes, mutates, and re-packs the textures payload in one pass. */
        private void paperarc$edit(java.util.function.Consumer<JsonObject> mutator) {
            JsonObject root = paperarc$decodeRoot();
            if (root == null) {
                root = new JsonObject();
                root.addProperty("timestamp", System.currentTimeMillis());
                root.addProperty("isPublic", true);
                root.add("textures", new JsonObject());
            } else if (!root.has("textures") || !root.get("textures").isJsonObject()) {
                root.add("textures", new JsonObject());
            }
            mutator.accept(root.getAsJsonObject("textures"));
            String packed = Base64.getEncoder()
                    .encodeToString(root.toString().getBytes(StandardCharsets.UTF_8));
            CraftPlayerProfile.this.profile.getProperties()
                    .put(TEXTURES_PROPERTY, new Property(TEXTURES_PROPERTY, packed));
        }

        @Override
        public boolean isEmpty() {
            JsonObject payload = paperarc$texturesOf(paperarc$decodeRoot());
            return payload == null || payload.entrySet().isEmpty();
        }

        @Override
        public void clear() {
            CraftPlayerProfile.this.profile.getProperties().removeAll(TEXTURES_PROPERTY);
        }

        @Override
        public URL getSkin() {
            JsonObject payload = paperarc$texturesOf(paperarc$decodeRoot());
            return paperarc$url(payload == null ? null
                    : payload.has("SKIN") && payload.get("SKIN").isJsonObject()
                            ? payload.getAsJsonObject("SKIN") : null);
        }

        @Override
        public void setSkin(URL skin) {
            setSkin(skin, getSkinModel());
        }

        @Override
        public void setSkin(URL skin, PlayerTextures.SkinModel model) {
            Preconditions.checkNotNull(skin, "skin");
            Preconditions.checkNotNull(model, "model");
            paperarc$edit(payload -> {
                JsonObject section = new JsonObject();
                section.addProperty("url", skin.toString());
                if (model == PlayerTextures.SkinModel.SLIM) {
                    JsonObject metadata = new JsonObject();
                    metadata.addProperty("model", "slim");
                    section.add("metadata", metadata);
                }
                payload.remove("SKIN");
                payload.add("SKIN", section);
            });
        }

        @Override
        public PlayerTextures.SkinModel getSkinModel() {
            JsonObject payload = paperarc$texturesOf(paperarc$decodeRoot());
            if (payload != null && payload.has("SKIN") && payload.get("SKIN").isJsonObject()) {
                JsonObject skin = payload.getAsJsonObject("SKIN");
                if (skin.has("metadata") && skin.get("metadata").isJsonObject()) {
                    JsonObject metadata = skin.getAsJsonObject("metadata");
                    if (metadata.has("model")
                            && "slim".equals(metadata.get("model").getAsString())) {
                        return PlayerTextures.SkinModel.SLIM;
                    }
                }
            }
            return PlayerTextures.SkinModel.CLASSIC;
        }

        @Override
        public URL getCape() {
            JsonObject payload = paperarc$texturesOf(paperarc$decodeRoot());
            return paperarc$url(payload == null ? null
                    : payload.has("CAPE") && payload.get("CAPE").isJsonObject()
                            ? payload.getAsJsonObject("CAPE") : null);
        }

        @Override
        public void setCape(URL cape) {
            if (cape == null) {
                paperarc$edit(payload -> payload.remove("CAPE"));
                return;
            }
            paperarc$edit(payload -> {
                JsonObject section = new JsonObject();
                section.addProperty("url", cape.toString());
                payload.remove("CAPE");
                payload.add("CAPE", section);
            });
        }

        @Override
        public long getTimestamp() {
            JsonObject root = paperarc$decodeRoot();
            if (root != null && root.has("timestamp")) {
                try {
                    return root.get("timestamp").getAsLong();
                } catch (Exception ignored) {
                    // fall through
                }
            }
            return 0L;
        }

        @Override
        public boolean isSigned() {
            Collection<Property> stored =
                    CraftPlayerProfile.this.profile.getProperties().get(TEXTURES_PROPERTY);
            return stored != null && !stored.isEmpty() && stored.iterator().next().hasSignature();
        }
    }
}
