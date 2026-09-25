package com.ixnah.mc.paperarc.bridge;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
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
import net.minecraft.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.profile.PlayerTextures;

/**
 * Minimal paper-api {@link com.destroystokyo.paper.profile.PlayerProfile}
 * implementation over an authlib {@link GameProfile} snapshot (blocked
 * batch 2 infrastructure; replaces the dynamic-proxy adapter previously
 * needed for {@code Skull#getPlayerProfile}).
 *
 * <p>Textures round-trip through the packed base64 {@code textures} property
 * exactly like Mojang ships them; locally built payloads (via
 * {@link #setTextures}) are unsigned. Completion resolves through the
 * server name/id cache; {@code complete(...)} additionally fills
 * properties from the {@code MinecraftSessionService}. {@link #update()} 走专用
 * 守护线程池，是真异步。</p>
 */
public class CraftPlayerProfile implements com.destroystokyo.paper.profile.PlayerProfile {

    private static final String TEXTURES_PROPERTY = "textures";

    /** 会话服务查询用的专用守护线程池（对齐 Paper 的 Util.PROFILE_EXECUTOR）。 */
    private static final java.util.concurrent.Executor PROFILE_EXECUTOR =
            java.util.concurrent.Executors.newCachedThreadPool(new java.util.concurrent.ThreadFactory() {
                private final java.util.concurrent.atomic.AtomicInteger counter =
                        new java.util.concurrent.atomic.AtomicInteger();

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "PaperArc Profile Updater #" + counter.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                }
            });

    /** authlib 7 的 GameProfile 是不可变 Record：id/name/属性自持，按需 build（同 CB 的 buildGameProfile）。 */
    private UUID id;
    private String name;
    private final Multimap<String, Property> properties = LinkedHashMultimap.create();

    public CraftPlayerProfile(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public CraftPlayerProfile(GameProfile gameProfile) {
        Preconditions.checkNotNull(gameProfile, "gameProfile");
        this.id = Util.NIL_UUID.equals(gameProfile.id()) ? null : gameProfile.id();
        this.name = gameProfile.name().isEmpty() ? null : gameProfile.name();
        if (gameProfile.properties() != null) {
            this.properties.putAll(gameProfile.properties());
        }
    }

    /** Wraps the given GameProfile (authlib 7 profiles are immutable, so this is a copy). */
    public static CraftPlayerProfile asBukkitMirror(GameProfile gameProfile) {
        return new CraftPlayerProfile(gameProfile);
    }

    /**
     * Universal conversion: any paper-api {@code PlayerProfile} implementation
     * becomes a fresh authlib {@link GameProfile} (deep property copy).
     */
    public static GameProfile asAuthlibCopy(com.destroystokyo.paper.profile.PlayerProfile profile) {
        Preconditions.checkNotNull(profile, "profile");
        Multimap<String, Property> props = LinkedHashMultimap.create();
        for (ProfileProperty property : profile.getProperties()) {
            props.put(property.getName(),
                    new Property(property.getName(), property.getValue(), property.getSignature()));
        }
        return paperarc$build(profile.getId(), profile.getName(), props);
    }

    private static GameProfile paperarc$build(UUID id, String name, Multimap<String, Property> props) {
        return new GameProfile(id != null ? id : Util.NIL_UUID, name != null ? name : "",
                new PropertyMap(LinkedHashMultimap.create(props)));
    }

    /** A freshly built authlib profile (same accessor Paper's CraftPlayerProfile exposes). */
    public GameProfile getGameProfile() {
        return paperarc$build(this.id, this.name, this.properties);
    }

    // ===== identity =====

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    public UUID setId(UUID uniqueId) {
        UUID previous = this.id;
        this.id = uniqueId;
        return previous;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String setName(String name) {
        String previous = this.name;
        this.name = name;
        return previous;
    }

    @Override
    public UUID getUniqueId() {
        return this.id;
    }

    // ===== properties =====

    @Override
    public Set<ProfileProperty> getProperties() {
        Set<ProfileProperty> out = new HashSet<>();
        for (Map.Entry<String, Property> entry : this.properties.entries()) {
            Property property = entry.getValue();
            out.add(new ProfileProperty(entry.getKey(), property.value(), property.signature()));
        }
        return out;
    }

    @Override
    public boolean hasProperty(String name) {
        Preconditions.checkNotNull(name, "name");
        return this.properties.containsKey(name);
    }

    @Override
    public void setProperty(ProfileProperty property) {
        Preconditions.checkNotNull(property, "property");
        this.properties.put(property.getName(),
                new Property(property.getName(), property.getValue(), property.getSignature()));
    }

    @Override
    public void setProperties(Collection<ProfileProperty> properties) {
        Preconditions.checkNotNull(properties, "properties");
        for (ProfileProperty property : properties) {
            setProperty(property);
        }
    }

    @Override
    public boolean removeProperty(String name) {
        Preconditions.checkNotNull(name, "name");
        return !this.properties.removeAll(name).isEmpty();
    }

    @Override
    public void clearProperties() {
        this.properties.clear();
    }

    @Override
    public boolean hasTextures() {
        return hasProperty(TEXTURES_PROPERTY);
    }

    // ===== completion =====

    @Override
    public boolean isComplete() {
        return this.id != null && this.name != null;
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
     * name/id cache ({@code Services#nameToIdCache}) — no network. {@code lookupName}: resolve the
     * name from the id; {@code lookupId}: resolve the id from the name.
     */
    @Override
    public boolean completeFromCache(boolean lookupName, boolean lookupId) {
        net.minecraft.server.MinecraftServer nms = paperarc$nmsServer();
        if (nms == null) {
            return isComplete();
        }
        if (getId() == null && getName() != null && lookupId) {
            nms.services().nameToIdCache().get(getName()).ifPresent(found -> this.id = found.id());
        } else if (getName() == null && getId() != null && lookupName) {
            nms.services().nameToIdCache().get(getId()).ifPresent(found -> this.name = found.name());
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
                    nms.services().sessionService().fetchProfile(getId(), requireSecure);
            if (result == null) {
                return false;
            }
            // authlib ProfileResult#profile() returns the GameProfile directly (not an Optional).
            GameProfile found = result.profile();
            if (found == null) {
                return false;
            }
            if (getName() == null && !found.name().isEmpty()) {
                this.name = found.name();
            }
            this.properties.putAll(found.properties());
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
     * Paper 语义：{@code update()} 是**异步**的 —— 会话服务查询是阻塞 HTTP 调用，
     * 必须扔到工作线程，future 由该线程完成。原实现在调用线程同步查完再返回一个
     * 已完成的 future，插件在主线程调用就是一次网络阻塞。
     *
     * <p>用专用守护线程池（对齐 Paper 的 {@code Util.PROFILE_EXECUTOR}）而不是
     * {@code ForkJoinPool.commonPool()}：这些任务是阻塞 IO，放进 common pool 会拖垮
     * 依赖它的并行流。{@code complete(...)} 保持同步语义不变。</p>
     */
    @Override
    public CompletableFuture<PlayerProfile> update() {
        return CompletableFuture.supplyAsync(() -> {
            paperarc$fillFromSessionService(true);
            return this;
        }, PROFILE_EXECUTOR);
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
        CraftPlayerProfile copy = new CraftPlayerProfile(this.id, this.name);
        copy.properties.putAll(this.properties);
        return copy;
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
        Multimap<String, Property> props = this.properties;
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
                    CraftPlayerProfile.this.properties.get(TEXTURES_PROPERTY);
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
            CraftPlayerProfile.this.properties
                    .put(TEXTURES_PROPERTY, new Property(TEXTURES_PROPERTY, packed));
        }

        @Override
        public boolean isEmpty() {
            JsonObject payload = paperarc$texturesOf(paperarc$decodeRoot());
            return payload == null || payload.entrySet().isEmpty();
        }

        @Override
        public void clear() {
            CraftPlayerProfile.this.properties.removeAll(TEXTURES_PROPERTY);
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
                    CraftPlayerProfile.this.properties.get(TEXTURES_PROPERTY);
            return stored != null && !stored.isEmpty() && stored.iterator().next().hasSignature();
        }
    }
}
