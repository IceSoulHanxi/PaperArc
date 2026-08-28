package com.ixnah.mc.paperarc.mixin.common.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.destroystokyo.paper.Title;
import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.util.TriState;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.world.effect.MobEffectInstance;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.DyeColor;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.bukkit.craftbukkit.v.util.CraftChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.ixnah.mc.paperarc.bridge.ApiState;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import io.papermc.paper.math.Position;

/**
 * Adds Paper player API additions on CraftPlayer (B31 part 2).
 *
 * PaperAdventure is unavailable in Arclight, so Adventure components are
 * converted via a Gson round-trip through {@code Component.Serializer};
 * bungee-chat components are serialized to JSON and parsed the same way.
 * State that vanilla 1.21.1 does not store per-player (Paper-side flags such
 * as affectsSpawning / flyingFallDamage / view-distance overrides) lives in
 * {@link ApiState}.
 */
@Mixin(CraftPlayer.class)
public abstract class CraftPlayerApiMixinPart2 {

    @Unique
    private static final String PAPERARC$KEY_PLAYER_LIST_NAME = "playerListName";
    @Unique
    private static final String PAPERARC$KEY_AFFECTS_SPAWNING = "affectsSpawning";
    @Unique
    private static final String PAPERARC$KEY_FLYING_FALL_DAMAGE = "flyingFallDamage";
    @Unique
    private static final String PAPERARC$KEY_SEND_VIEW_DISTANCE = "sendViewDistance";
    @Unique
    private static final String PAPERARC$KEY_SIMULATION_DISTANCE = "simulationDistance";

    @Shadow
    public abstract ServerPlayer getHandle();

    @Shadow
    public abstract String getPlayerListName();

    @Shadow
    public abstract void setPlayerListName(String playerListName);

    @Shadow
    public abstract void sendSignChange(Location location, String[] lines, DyeColor dyeColor, boolean hasGlowingText);

    @Shadow
    private net.minecraft.network.chat.Component playerListHeader;

    @Shadow
    private net.minecraft.network.chat.Component playerListFooter;

    @Shadow
    private void updatePlayerListHeaderFooter() {
        throw new AssertionError();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    @Unique
    private static net.minecraft.server.MinecraftServer paperarc$nmsServer() {
        return ((CraftServer) PaperArcBridge.getServer()).getServer();
    }

    @Unique
    private void paperarc$send(Packet<?> packet) {
        getHandle().connection.send(packet);
    }

    @Unique
    private net.minecraft.network.chat.Component paperarc$vanilla(Component adventure) {
        if (adventure == null) {
            return null;
        }
        return Serializer.fromJson(GsonComponentSerializer.gson().serialize(adventure));
    }

    @Unique
    private Component paperarc$adventure(net.minecraft.network.chat.Component vanilla) {
        if (vanilla == null) {
            return Component.empty();
        }
        return GsonComponentSerializer.gson().deserialize(
                Serializer.toJson(vanilla));
    }

    @Unique
    private net.minecraft.network.chat.Component paperarc$bungee(BaseComponent[] comps) {
        if (comps == null || comps.length == 0) {
            return null;
        }
        return Serializer.fromJson(ComponentSerializer.toString(comps));
    }

    @Unique
    private net.minecraft.world.entity.monster.warden.WardenSpawnTracker paperarc$wardenTracker() {
        return getHandle().getWardenSpawnTracker().orElse(null);
    }

    @Unique
    private static void paperarc$setIntField(Class<?> clazz, Object target, String name, int value)
            throws ReflectiveOperationException {
        Field f = clazz.getDeclaredField(name);
        f.setAccessible(true);
        f.setInt(target, value);
    }

    @Unique
    private static String paperarc$sha1Hex(byte[] hash) {
        if (hash == null || hash.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    @Unique
    public net.kyori.adventure.text.Component playerListHeader() {
        return paperarc$adventure(playerListHeader);
    }

    @Unique
    public net.kyori.adventure.text.Component playerListName() {
        Component stored = ApiState.get(this, PAPERARC$KEY_PLAYER_LIST_NAME, null);
        if (stored != null) {
            return stored;
        }
        String legacy = getPlayerListName();
        return legacy == null || legacy.isEmpty() ? Component.empty() : Component.text(legacy);
    }

    @Unique
    public void playerListName(net.kyori.adventure.text.Component playerListName) {
        ApiState.put(this, PAPERARC$KEY_PLAYER_LIST_NAME, playerListName);
        // Best-effort: mirror into vanilla Player.listName (private) so future
        // tab-list packets carry the new name.
        try {
            java.lang.reflect.Field f = net.minecraft.world.entity.player.Player.class.getDeclaredField("listName");
            f.setAccessible(true);
            f.set(getHandle(), paperarc$vanilla(playerListName));
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @Unique
    public void removeAdditionalChatCompletions(java.util.Collection<String> completions) {
        if (completions == null || completions.isEmpty()) {
            return;
        }
        List<String> names = new ArrayList<>();
        for (Object o : completions) {
            if (o instanceof String s && !s.isEmpty()) {
                names.add(s);
            }
        }
        if (!names.isEmpty()) {
            paperarc$send(new ClientboundCustomChatCompletionsPacket(
                    ClientboundCustomChatCompletionsPacket.Action.REMOVE, names));
        }
    }

    @Unique
    public void resetCooldown() {
        getHandle().resetAttackStrengthTicker();
    }

    @Unique
    public void resetIdleDuration() {
        getHandle().resetLastActionTime();
    }

    @Unique
    public void sendActionBar(char alternateChar, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        net.minecraft.network.chat.Component vanilla =
                CraftChatMessage.fromStringOrNull(message.replace(alternateChar, '\u00a7'));
        if (vanilla != null) {
            paperarc$send(new ClientboundSetActionBarTextPacket(vanilla));
        }
    }

    @Unique
    public void sendActionBar(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        net.minecraft.network.chat.Component vanilla = CraftChatMessage.fromStringOrNull(message);
        if (vanilla != null) {
            paperarc$send(new ClientboundSetActionBarTextPacket(vanilla));
        }
    }

    @Unique
    public void sendActionBar(BaseComponent[] message) {
        net.minecraft.network.chat.Component vanilla = paperarc$bungee(message);
        if (vanilla != null) {
            paperarc$send(new ClientboundSetActionBarTextPacket(vanilla));
        }
    }

    @Unique
    public void sendMultiBlockChange(Map<Position, BlockData> blockChanges) {
        if (blockChanges == null) {
            return;
        }
        for (Map.Entry<?, ?> e : blockChanges.entrySet()) {
            if (!(e.getValue() instanceof BlockData data)) {
                continue;
            }
            BlockPos pos = null;
            Object key = e.getKey();
            if (key instanceof Position position) {
                pos = new BlockPos(position.blockX(), position.blockY(), position.blockZ());
            } else if (key instanceof Location location) {
                pos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            }
            if (pos != null && e.getValue() instanceof CraftBlockData craftData) {
                paperarc$send(new ClientboundBlockUpdatePacket(pos, craftData.getState()));
            }
        }
    }

    @Unique
    public void sendOpLevel(byte level) {
        paperarc$nmsServer().getPlayerList().broadcastAll(
                new ClientboundEntityEventPacket(getHandle(), level));
    }

    @Unique
    public void sendSignChange(Location location, java.util.List<String> lines, DyeColor dyeColor,
            boolean hasGlowingText) {
        if (lines != null && lines.size() > 4) {
            throw new IllegalArgumentException("Cannot send more than 4 sign lines");
        }
        String[] arr = new String[4];
        if (lines != null) {
            for (int i = 0; i < lines.size(); i++) {
                arr[i] = lines.get(i);
            }
        }
        sendSignChange(location, arr, dyeColor, hasGlowingText);
    }

    @Unique
    public void sendTitle(Title title) {
        if (title == null) {
            return;
        }
        net.minecraft.network.chat.Component main = paperarc$bungee(title.getTitle());
        if (main != null) {
            paperarc$send(new ClientboundSetTitleTextPacket(main));
        }
        net.minecraft.network.chat.Component sub = paperarc$bungee(title.getSubtitle());
        if (sub != null) {
            paperarc$send(new ClientboundSetSubtitleTextPacket(sub));
        }
        paperarc$send(new ClientboundSetTitlesAnimationPacket(title.getFadeIn(), title.getStay(), title.getFadeOut()));
    }

    @Unique
    public void setAffectsSpawning(boolean affects) {
        ApiState.put(this, PAPERARC$KEY_AFFECTS_SPAWNING, affects);
    }

    @Unique
    public void setExperienceLevelAndProgress(int totalExperience) {
        ServerPlayer handle = getHandle();
        handle.experienceLevel = 0;
        handle.experienceProgress = 0.0F;
        handle.totalExperience = totalExperience;
    }

    @Unique
    public void setFlyingFallDamage(TriState triState) {
        ApiState.put(this, PAPERARC$KEY_FLYING_FALL_DAMAGE, triState);
    }

    @Unique
    public void setHasSeenWinScreen(boolean hasSeenWinScreen) {
        getHandle().seenCredits = hasSeenWinScreen;
    }

    @Unique
    public void setPlayerListHeaderFooter(BaseComponent header, BaseComponent footer) {
        playerListHeader = header == null ? null : paperarc$bungee(new BaseComponent[]{header});
        playerListFooter = footer == null ? null : paperarc$bungee(new BaseComponent[]{footer});
        updatePlayerListHeaderFooter();
    }

    @Unique
    public void setPlayerListHeaderFooter(BaseComponent[] header, BaseComponent[] footer) {
        playerListHeader = paperarc$bungee(header);
        playerListFooter = paperarc$bungee(footer);
        updatePlayerListHeaderFooter();
    }

    /**
     * Paper parity for {@code Player#setPlayerProfile(PlayerProfile)}: swaps
     * {@code Player.gameProfile} (private final in vanilla NMS — reflective
     * swap through a cached Field, javap-verified), then replays Paper's
     * client-sync logic from patches/server/Player.setPlayerProfile-API.patch:
     * tab-list resync plus entity re-track for every viewer (fresh skins and
     * nametags), and the {@code refreshPlayer()} respawn pipeline for the
     * target client itself. Vanilla 1.21.1 has no Paper
     * {@code ServerPlayer#sentListPacket} flag, so the silent-swap shortcut
     * guards on {@code connection != null} instead.
     */
    @Unique
    public void setPlayerProfile(com.destroystokyo.paper.profile.PlayerProfile profile) {
        Preconditions.checkNotNull(profile, "profile");
        ServerPlayer self = getHandle();
        // asAuthlibCopy accepts ANY paper-api PlayerProfile implementation
        // (Paper parity): deep-copies id/name/properties into a GameProfile.
        GameProfile gameProfile = com.ixnah.mc.paperarc.bridge.CraftPlayerProfile.asAuthlibCopy(profile);
        try {
            paperarc$gameProfileField().set(self, gameProfile);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to swap ServerPlayer GameProfile", e);
        }
        if (self.connection == null) {
            return;
        }
        // Refresh other viewers: new tab-list entry + entity re-track.
        for (ServerPlayer other : paperarc$nmsServer().getPlayerList().getPlayers()) {
            if (other == self || other.connection == null) {
                continue;
            }
            other.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(java.util.List.of(self)));
            // ChunkMap.TrackedEntity is a private nested type in vanilla 1.21.1
            // (javap-verified) — hold it as Object and invoke reflectively.
            Object entry = paperarc$trackedEntity(other, self.getId());
            if (entry != null) {
                paperarc$invokeTracker(entry, "removePlayer", other);
                paperarc$invokeTracker(entry, "updatePlayer", other);
            }
        }
        // Refresh the target client: Paper's refreshPlayer() respawn pipeline.
        // 1.20.1 无 createCommonSpawnInfo——用 Player.setPlayerProfile-API.patch 的 10 参构造。
        net.minecraft.server.level.ServerLevel worldserver = self.serverLevel();
        self.connection.send(new net.minecraft.network.protocol.game.ClientboundRespawnPacket(
                worldserver.dimensionTypeId(), worldserver.dimension(),
                net.minecraft.world.level.biome.BiomeManager.obfuscateSeed(worldserver.getSeed()),
                self.gameMode.getGameModeForPlayer(), self.gameMode.getPreviousGameModeForPlayer(),
                worldserver.isDebug(), worldserver.isFlat(),
                net.minecraft.network.protocol.game.ClientboundRespawnPacket.KEEP_ALL_DATA,
                self.getLastDeathLocation(), self.getPortalCooldown()));
        self.onUpdateAbilities();
        Location loc = ((CraftPlayer) (Object) this).getLocation();
        self.connection.teleport(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch(),
                java.util.Collections.emptySet());
        net.minecraft.server.players.PlayerList playerList = paperarc$nmsServer().getPlayerList();
        playerList.sendPlayerPermissionLevel(self);
        playerList.sendLevelInfo(self, worldserver);
        playerList.sendAllPlayerInfo(self);
        self.connection.send(new net.minecraft.network.protocol.game.ClientboundSetExperiencePacket(
                self.experienceProgress, self.totalExperience, self.experienceLevel));
        for (net.minecraft.world.effect.MobEffectInstance mobEffect : self.getActiveEffects()) {
            self.connection.send(new net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket(
                    self.getId(), mobEffect));
        }
    }

    /** Cached reflection handle on the private-final {@code Player.gameProfile} field. */
    private static volatile Field PAPERARC$GAME_PROFILE_FIELD;

    @Unique
    private static Field paperarc$gameProfileField() throws ReflectiveOperationException {
        Field field = PAPERARC$GAME_PROFILE_FIELD;
        if (field == null) {
            field = net.minecraft.world.entity.player.Player.class.getDeclaredField("gameProfile");
            field.setAccessible(true);
            PAPERARC$GAME_PROFILE_FIELD = field;
        }
        return field;
    }

    private static volatile Field PAPERARC$ENTITY_MAP_FIELD;

    @Unique
    private static Field paperarc$entityMapField() throws ReflectiveOperationException {
        Field field = PAPERARC$ENTITY_MAP_FIELD;
        if (field == null) {
            field = net.minecraft.server.level.ChunkMap.class.getDeclaredField("entityMap");
            field.setAccessible(true);
            PAPERARC$ENTITY_MAP_FIELD = field;
        }
        return field;
    }

    /**
     * The tracker entry of {@code target} inside {@code viewer}'s level chunk
     * map (private fastutil map, reflectively read once cached); null when
     * the target is not tracked by that viewer.
     */
    @Unique
    private static Object paperarc$trackedEntity(
            ServerPlayer viewer, int targetId) {
        try {
            Object map = paperarc$entityMapField().get(
                    ((net.minecraft.server.level.ServerLevel) viewer.level()).getChunkSource().chunkMap);
            Object entry = ((it.unimi.dsi.fastutil.ints.Int2ObjectMap<?>) map).get(targetId);
            return entry;
        } catch (ReflectiveOperationException | ClassCastException e) {
            return null;
        }
    }

    /** Reflective {@code ChunkMap.TrackedEntity#removePlayer/updatePlayer} (private nested class). */
    @Unique
    private static void paperarc$invokeTracker(Object entry, String name, ServerPlayer viewer) {
        try {
            java.lang.reflect.Method method = entry.getClass()
                    .getMethod(name, net.minecraft.server.level.ServerPlayer.class);
            method.setAccessible(true);
            method.invoke(entry, viewer);
        } catch (ReflectiveOperationException ignored) {
            // viewer no longer tracks the target — nothing to refresh
        }
    }

    @Unique
    public void setResourcePack(java.util.UUID id, String url, byte[] hash,
            net.kyori.adventure.text.Component prompt, boolean force) {
        paperarc$send(new net.minecraft.network.protocol.game.ClientboundResourcePackPacket(
                url, paperarc$sha1Hex(hash), force, paperarc$vanilla(prompt)));
    }

    @Unique
    public void setSendViewDistance(int viewDistance) {
        ApiState.put(this, PAPERARC$KEY_SEND_VIEW_DISTANCE, viewDistance);
    }

    @Unique
    public void setSimulationDistance(int simulationDistance) {
        ApiState.put(this, PAPERARC$KEY_SIMULATION_DISTANCE, simulationDistance);
    }

    @Unique
    public void setViewDistance(int viewDistance) {
        try {
            paperarc$setIntField(ServerPlayer.class, getHandle(), "requestedViewDistance", viewDistance);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @Unique
    public void setSubtitle(BaseComponent subtitle) {
        if (subtitle == null) {
            return;
        }
        paperarc$send(new ClientboundSetSubtitleTextPacket(paperarc$bungee(new BaseComponent[]{subtitle})));
    }

    @Unique
    public void setSubtitle(BaseComponent[] subtitle) {
        net.minecraft.network.chat.Component vanilla = paperarc$bungee(subtitle);
        if (vanilla != null) {
            paperarc$send(new ClientboundSetSubtitleTextPacket(vanilla));
        }
    }

    @Unique
    public void setTitleTimes(int fadeInTicks, int stayTicks, int fadeOutTicks) {
        paperarc$send(new ClientboundSetTitlesAnimationPacket(fadeInTicks, stayTicks, fadeOutTicks));
    }

    @Unique
    public void setWardenTimeSinceLastWarning(int time) {
        net.minecraft.world.entity.monster.warden.WardenSpawnTracker tracker = paperarc$wardenTracker();
        if (tracker == null) {
            return;
        }
        try {
            paperarc$setIntField(tracker.getClass(), tracker, "ticksSinceLastWarning", time);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @Unique
    public void setWardenWarningCooldown(int cooldown) {
        net.minecraft.world.entity.monster.warden.WardenSpawnTracker tracker = paperarc$wardenTracker();
        if (tracker == null) {
            return;
        }
        try {
            paperarc$setIntField(tracker.getClass(), tracker, "cooldownTicks", cooldown);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @Unique
    public void setWardenWarningLevel(int warningLevel) {
        net.minecraft.world.entity.monster.warden.WardenSpawnTracker tracker = paperarc$wardenTracker();
        if (tracker == null) {
            return;
        }
        try {
            paperarc$setIntField(tracker.getClass(), tracker, "warningLevel", warningLevel);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @Unique
    public void showElderGuardian(boolean show) {
        // vanilla entity events 47/48: elder guardian mob effect appears / fades
        paperarc$send(new ClientboundEntityEventPacket(getHandle(), (byte) (show ? 47 : 48)));
    }

    @Unique
    public void showTitle(BaseComponent title) {
        if (title == null) {
            return;
        }
        paperarc$send(new ClientboundSetTitleTextPacket(paperarc$bungee(new BaseComponent[]{title})));
    }

    @Unique
    public void showTitle(BaseComponent title, BaseComponent subtitle, int fadeInTicks, int stayTicks,
            int fadeOutTicks) {
        if (title != null) {
            paperarc$send(new ClientboundSetTitleTextPacket(paperarc$bungee(new BaseComponent[]{title})));
        }
        net.minecraft.network.chat.Component sub = paperarc$bungee(
                subtitle == null ? null : new BaseComponent[]{subtitle});
        if (sub != null) {
            paperarc$send(new ClientboundSetSubtitleTextPacket(sub));
        }
        paperarc$send(new ClientboundSetTitlesAnimationPacket(fadeInTicks, stayTicks, fadeOutTicks));
    }

    @Unique
    public void showTitle(BaseComponent[] title) {
        net.minecraft.network.chat.Component vanilla = paperarc$bungee(title);
        if (vanilla != null) {
            paperarc$send(new ClientboundSetTitleTextPacket(vanilla));
        }
    }

    @Unique
    public void showTitle(BaseComponent[] title, BaseComponent[] subtitle, int fadeInTicks, int stayTicks,
            int fadeOutTicks) {
        net.minecraft.network.chat.Component main = paperarc$bungee(title);
        if (main != null) {
            paperarc$send(new ClientboundSetTitleTextPacket(main));
        }
        net.minecraft.network.chat.Component sub = paperarc$bungee(subtitle);
        if (sub != null) {
            paperarc$send(new ClientboundSetSubtitleTextPacket(sub));
        }
        paperarc$send(new ClientboundSetTitlesAnimationPacket(fadeInTicks, stayTicks, fadeOutTicks));
    }

    @Unique
    public void showWinScreen() {
        paperarc$send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 0.0F));
    }

    @Unique
    public boolean unlistPlayer(org.bukkit.entity.Player player) {
        Preconditions.checkArgument(player != null, "player must not be null");
        if (!(player instanceof CraftPlayer) || getHandle().connection == null) {
            return false;
        }
        ServerPlayer other = ((CraftPlayer) player).getHandle();
        ClientboundPlayerInfoUpdatePacket packet = new ClientboundPlayerInfoUpdatePacket(
                EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED), List.of(other));
        try {
            List<ClientboundPlayerInfoUpdatePacket.Entry> entries =
                    new ArrayList<>(packet.entries());
            if (entries.isEmpty()) {
                return false;
            }
            // rebuild the sole entry with listed=false (packet ctor always sets true)
            ClientboundPlayerInfoUpdatePacket.Entry old = entries.get(0);
            entries.set(0, new ClientboundPlayerInfoUpdatePacket.Entry(old.profileId(), old.profile(),
                    false, old.latency(), old.gameMode(), old.displayName(), old.chatSession()));
            java.lang.reflect.Field f = ClientboundPlayerInfoUpdatePacket.class.getDeclaredField("entries");
            f.setAccessible(true);
            f.set(packet, entries);
            paperarc$send(packet);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Unique
    public void updateTitle(Title title) {
        if (title == null) {
            return;
        }
        paperarc$send(new ClientboundSetTitlesAnimationPacket(title.getFadeIn(), title.getStay(), title.getFadeOut()));
    }

    // PAPERARC$APPEND_MARKER
}
