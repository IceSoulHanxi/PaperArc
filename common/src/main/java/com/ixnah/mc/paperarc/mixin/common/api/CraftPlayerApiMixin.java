package com.ixnah.mc.paperarc.mixin.common.api;

import com.destroystokyo.paper.ClientOption;
import com.destroystokyo.paper.Title;
import com.google.common.base.Preconditions;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import com.mojang.authlib.GameProfile;
import io.papermc.paper.math.Position;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.util.TriState;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.Util;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.bukkit.DyeColor;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.craftbukkit.v.entity.CraftPlayer;
import org.bukkit.craftbukkit.v.util.CraftChatMessage;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.inventory.MainHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * CraftPlayer 的 paper-api 扩展（原批次 B30 + B31 两个切片，2026-09-17 合并为一个
 * mixin —— 拆成 Part1/Part2 只是当初为了并行写代码）。
 *
 * <p>PaperAdventure is unavailable in Arclight, so Adventure components are
 * converted via a Gson round-trip through {@code Component.Serializer};
 * bungee-chat components are serialized to JSON and parsed the same way.
 * State that vanilla 1.21.1 does not store per-player (Paper-side flags such
 * as affectsSpawning / flyingFallDamage / view-distance overrides) lives in
 * {@link ApiState}.</p>
 */
@Mixin(CraftPlayer.class)
public abstract class CraftPlayerApiMixin {

    /** Paper 侧补充状态（原 ApiState 副表键 "displayName"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private Component paperarc$displayName;

    /** Paper 侧补充状态（原 ApiState 副表键 "affectsSpawning"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private Boolean paperarc$affectsSpawning;

    /** Paper 侧补充状态（原 ApiState 副表键 "clientBrandName"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private String paperarc$clientBrandName;

    /** Paper 侧补充状态（原 ApiState 副表键 "haProxyAddress"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private InetSocketAddress paperarc$haProxyAddress;

    /** Paper 侧补充状态（原 ApiState 副表键 "resourcePackStatus"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private PlayerResourcePackStatusEvent.Status paperarc$resourcePackStatus;

    /** Paper 侧补充状态（原 ApiState 副表键 "sendViewDistance"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private Integer paperarc$sendViewDistance;

    /** Paper 侧补充状态（原 ApiState 副表键 "playerListName"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private net.kyori.adventure.text.Component paperarc$playerListName;

    /** Paper 侧补充状态（原 ApiState 副表键 "flyingFallDamage"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private TriState paperarc$flyingFallDamage;

    /** Paper 的 tab-list 隐藏名单（Paper 用 ServerPlayer 上的 Set，这里放在 CraftPlayer 上等价）。 */
    @Unique
    private final java.util.Set<java.util.UUID> paperarc$unlisted = new java.util.HashSet<>();

    /** Paper 侧补充状态（原 ApiState 副表键 "simulationDistance"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private Integer paperarc$simulationDistance;

    @Shadow
    public abstract ServerPlayer getHandle();

    @Shadow
    public abstract String getDisplayName();

    @Shadow
    public abstract void setDisplayName(String displayName);

    @Unique
    private static PlayerList paperarc$playerList() {
        return ((org.bukkit.craftbukkit.v.CraftServer) org.bukkit.Bukkit.getServer()).getServer().getPlayerList();
    }

    // ---- activeBossBars ----
    @Unique
    public Iterable activeBossBars() {
        // vanilla 服务端不维护每玩家 BossBar 注册表（龙/凋灵血条无法反向回查），保守返回空集合
        return Collections.emptyList();
    }
    @Unique
    public void addAdditionalChatCompletions(Collection completions) {
        this.getHandle().connection.send(new ClientboundCustomChatCompletionsPacket(
            ClientboundCustomChatCompletionsPacket.Action.ADD, new ArrayList<>(completions)));
    }

    // ---- applyMending ----
    @Unique
    public int applyMending(int amount) {
        ServerPlayer sp = this.getHandle();
        sp.resetLastActionTime();
        int remaining = amount;
        for (int guard = 0; guard < 64 && remaining > 0; guard++) {
            Optional<EnchantedItemInUse> opt = EnchantmentHelper.getRandomItemWith(
                EnchantmentEffectComponents.REPAIR_WITH_XP, sp, ItemStack::isDamaged);
            if (opt.isEmpty()) {
                break;
            }
            ItemStack stack = opt.get().itemStack();
            if (stack.isEmpty() || !stack.isDamaged()) {
                break;
            }
            int want = EnchantmentHelper.modifyDurabilityToRepairFromXp(sp.serverLevel(), stack, remaining * 2);
            int heal = Math.min(want, stack.getDamageValue());
            if (heal <= 0) {
                break;
            }
            stack.setDamageValue(stack.getDamageValue() - heal);
            remaining -= heal / 2;
        }
        return Math.max(remaining, 0);
    }

    // ---- calculateTotalExperiencePoints ----
    @Unique
    public int calculateTotalExperiencePoints() {
        return this.getHandle().totalExperience;
    }

    // ---- displayName getter/setter ----
    @Unique
    public Component displayName() {
        Component stored = (this.paperarc$displayName != null ? this.paperarc$displayName : (null));
        if (stored != null) {
            return stored;
        }
        String legacy = this.getDisplayName();
        return legacy == null ? Component.empty()
            : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public void displayName(Component component) {
        this.paperarc$displayName = component;
        this.setDisplayName(component == null ? null
            : LegacyComponentSerializer.legacySection().serialize(component));
    }

    // ---- getAffectsSpawning ----
    @Unique
    public boolean getAffectsSpawning() {
        Boolean flag = (this.paperarc$affectsSpawning != null ? this.paperarc$affectsSpawning : (null));
        return flag == null || flag;
    }

    // ---- getClientBrandName ----
    @Unique
    public String getClientBrandName() {
        // vanilla 服务端不持久化客户端 brand（MC|Brand 即弃），需上层注入 ApiState
        return (this.paperarc$clientBrandName != null ? this.paperarc$clientBrandName : (null));
    }

    // ---- getClientOption ----
    @Unique
    public Object getClientOption(ClientOption option) {
        net.minecraft.server.level.ClientInformation info = this.getHandle().clientInformation();
        if (option == ClientOption.SKIN_PARTS) {
            return paperarc$skinParts((byte) info.modelCustomisation());
        }
        if (option == ClientOption.CHAT_VISIBILITY) {
            return ClientOption.ChatVisibility.valueOf(info.chatVisibility().name());
        }
        if (option == ClientOption.CHAT_COLORS_ENABLED) {
            return info.chatColors();
        }
        if (option == ClientOption.LOCALE) {
            String lang = info.language();
            return lang == null ? null : java.util.Locale.forLanguageTag(lang.replace('_', '-'));
        }
        if (option == ClientOption.VIEW_DISTANCE) {
            return info.viewDistance();
        }
        if (option == ClientOption.TEXT_FILTERING_ENABLED) {
            return info.textFilteringEnabled();
        }
        if (option == ClientOption.MAIN_HAND) {
            return MainHand.valueOf(info.mainHand().name());
        }
        if (option == ClientOption.ALLOW_SERVER_LISTINGS) {
            return info.allowsListing();
        }
        // 未知/无数据选项（如粒子可见性）返回 null
        return null;
    }

    // ---- getCooldownPeriod / getCooledAttackStrength ----
    @Unique
    public float getCooldownPeriod() {
        return this.getHandle().getCurrentItemAttackStrengthDelay();
    }

    @Unique
    public float getCooledAttackStrength(float adjustTicks) {
        return this.getHandle().getAttackStrengthScale(adjustTicks);
    }

    // ---- getExperiencePointsNeededForNextLevel ----
    @Unique
    public int getExperiencePointsNeededForNextLevel() {
        return this.getHandle().getXpNeededForNextLevel();
    }

    // ---- getHAProxyAddress ----
    @Unique
    public InetSocketAddress getHAProxyAddress() {
        // 需要 HAProxy proxy-protocol 基建在握手期保存真实地址，当前仅 side-map
        return (this.paperarc$haProxyAddress != null ? this.paperarc$haProxyAddress : (null));
    }

    // ---- getIdleDuration ----
    @Unique
    public Duration getIdleDuration() {
        return Duration.ofMillis(Math.max(0L, Util.getMillis() - this.getHandle().getLastActionTime()));
    }

    // ---- getResourcePackStatus ----
    @Unique
    public PlayerResourcePackStatusEvent.Status getResourcePackStatus() {
        // spigot 收到资源包状态后只发事件不存储，需上层在事件里回填 ApiState
        return (this.paperarc$resourcePackStatus != null ? this.paperarc$resourcePackStatus : (null));
    }

    // ---- getSendViewDistance ----
    @Unique
    public int getSendViewDistance() {
        Integer override = (this.paperarc$sendViewDistance != null ? this.paperarc$sendViewDistance : (null));
        if (override != null) {
            return override;
        }
        // vanilla 无每玩家发送距离，回退服务器级 view distance
        return paperarc$playerList().getViewDistance();
    }

    // ---- getSentChunkKeys ----
    @Unique
    public Set getSentChunkKeys() {
        // 需要 PlayerChunkLoader 发送队列基建（Paper 内部），保守返回空集合
        return new HashSet();
    }

    // ---- getSentChunks ----
    @Unique
    public Set getSentChunks() {
        // 同上，无 chunk 追踪基建，保守返回空集合
        return Collections.emptySet();
    }

    // ---- getSimulationDistance ----
    @Unique
    public int getSimulationDistance() {
        // vanilla 无每玩家模拟距离，回退服务器级 simulation distance
        return paperarc$playerList().getSimulationDistance();
    }

    // ---- helpers ----
    @Unique
    private static com.destroystokyo.paper.SkinParts paperarc$skinParts(final byte raw) {
        // 实现体在 bridge/：mixin 包内的（含匿名）类被合并后的 CraftPlayer 字节码引用即
        // IllegalClassLoadError，且内嵌类的 InnerClasses 属性会与目标类互相矛盾。
        return new com.ixnah.mc.paperarc.bridge.PaperArcSkinParts(raw);
    }

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
        return Serializer.fromJson(GsonComponentSerializer.gson().serialize(adventure),
                paperarc$nmsServer().registryAccess());
    }

    @Unique
    private Component paperarc$adventure(net.minecraft.network.chat.Component vanilla) {
        if (vanilla == null) {
            return Component.empty();
        }
        return GsonComponentSerializer.gson().deserialize(
                Serializer.toJson(vanilla, paperarc$nmsServer().registryAccess()));
    }

    @Unique
    private net.minecraft.network.chat.Component paperarc$bungee(BaseComponent[] comps) {
        if (comps == null || comps.length == 0) {
            return null;
        }
        return Serializer.fromJson(ComponentSerializer.toString(comps), paperarc$nmsServer().registryAccess());
    }

    @Unique
    private net.minecraft.world.entity.monster.warden.WardenSpawnTracker paperarc$wardenTracker() {
        return getHandle().getWardenSpawnTracker().orElse(null);
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
        Component stored = (this.paperarc$playerListName != null ? this.paperarc$playerListName : (null));
        if (stored != null) {
            return stored;
        }
        String legacy = getPlayerListName();
        return legacy == null || legacy.isEmpty() ? Component.empty() : Component.text(legacy);
    }

    @Unique
    public void playerListName(net.kyori.adventure.text.Component playerListName) {
        this.paperarc$playerListName = playerListName;
        // Best-effort: 镜像进 listName，让后续 tab-list 包带上新名字。
        // 该字段是 Arclight 的 ServerPlayerMixin 注入在 **ServerPlayer** 上的
        // （不是 vanilla 成员、不参与重映射），按 B2-1 的分类保留反射。
        // 注意：原实现反射的是 Player.class，字段根本不在那里，所以一直静默失败。
        try {
            java.lang.reflect.Field f = ServerPlayer.class.getDeclaredField("listName");
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
    public void sendEntityEffect(EntityEffect effect, org.bukkit.entity.Entity entity) {
        Preconditions.checkArgument(effect.isApplicableTo(entity), "%s is not applicable to %s", effect, entity);
        paperarc$send(new ClientboundEntityEventPacket(((CraftEntity) entity).getHandle(), effect.getData()));
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
        this.paperarc$affectsSpawning = affects;
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
        this.paperarc$flyingFallDamage = triState;
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
        self.gameProfile = gameProfile;
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
            net.minecraft.server.level.ChunkMap.TrackedEntity entry = paperarc$trackedEntity(other, self.getId());
            if (entry != null) {
                entry.removePlayer(other);
                entry.updatePlayer(other);
            }
        }
        // Refresh the target client: Paper's refreshPlayer() respawn pipeline.
        net.minecraft.server.level.ServerLevel worldserver = self.serverLevel();
        self.connection.send(new ClientboundRespawnPacket(
                self.createCommonSpawnInfo(worldserver), ClientboundRespawnPacket.KEEP_ALL_DATA));
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
                    self.getId(), mobEffect, false));
        }
    }

    /**
     * The tracker entry of {@code target} inside {@code viewer}'s level chunk map;
     * {@code ChunkMap.entityMap} 与 package-private 的 {@code ChunkMap$TrackedEntity}
     * 都由 {@code paperarc.accesswidener} 放开。null = 该 viewer 没在追踪目标。
     */
    @Unique
    private static net.minecraft.server.level.ChunkMap.TrackedEntity paperarc$trackedEntity(
            ServerPlayer viewer, int targetId) {
        if (!(viewer.level() instanceof net.minecraft.server.level.ServerLevel level)) {
            return null;
        }
        return level.getChunkSource().chunkMap.entityMap.get(targetId);
    }

    @Unique
    public void setResourcePack(java.util.UUID id, String url, byte[] hash,
            net.kyori.adventure.text.Component prompt, boolean force) {
        paperarc$send(new net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket(
                id, url, paperarc$sha1Hex(hash), force, java.util.Optional.ofNullable(paperarc$vanilla(prompt))));
    }

    @Unique
    public void setSendViewDistance(int viewDistance) {
        this.paperarc$sendViewDistance = viewDistance;
    }

    @Unique
    public void setSimulationDistance(int simulationDistance) {
        this.paperarc$simulationDistance = simulationDistance;
    }

    @Unique
    public void setViewDistance(int viewDistance) {
        getHandle().requestedViewDistance = viewDistance;
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
        tracker.ticksSinceLastWarning = time;
    }

    @Unique
    public void setWardenWarningCooldown(int cooldown) {
        net.minecraft.world.entity.monster.warden.WardenSpawnTracker tracker = paperarc$wardenTracker();
        if (tracker == null) {
            return;
        }
        tracker.cooldownTicks = cooldown;
    }

    @Unique
    public void setWardenWarningLevel(int warningLevel) {
        net.minecraft.world.entity.monster.warden.WardenSpawnTracker tracker = paperarc$wardenTracker();
        if (tracker == null) {
            return;
        }
        tracker.warningLevel = warningLevel;
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
        if (!this.paperarc$unlisted.add(player.getUniqueId())) {
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
            // entries 是 private final，由 paperarc.accesswidener 的 accessible + mutable 放开
            packet.entries = entries;
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

    // ===== Identified / NetworkClient（paper-api Player 的父接口，见 B4b）=====

    @Unique
    public net.kyori.adventure.identity.Identity identity() {
        return net.kyori.adventure.identity.Identity.identity(getHandle().getUUID());
    }

    /**
     * {@code com.destroystokyo.paper.network.NetworkClient}：握手包的协议号与虚拟主机由
     * {@code ServerHandshakeNetworkClientMixin} 记到 {@code Connection}（Paper 补充字段，
     * 经 {@link com.ixnah.mc.paperarc.bridge.ConnectionBridge} 读回）。Arclight 本身
     * 只把 {@code hostName:port} 拼成字符串交给 Spigot 的 PlayerHandshakeEvent，协议号直接丢弃。
     * 连接已断开时按 Paper 语义返回 -1 / null。
     */
    @Unique
    public int getProtocolVersion() {
        net.minecraft.server.network.ServerGamePacketListenerImpl listener = getHandle().connection;
        return listener == null ? -1
                : ((com.ixnah.mc.paperarc.bridge.ConnectionBridge) listener.connection).paper$getProtocolVersion();
    }

    @Unique
    public java.net.InetSocketAddress getVirtualHost() {
        net.minecraft.server.network.ServerGamePacketListenerImpl listener = getHandle().connection;
        return listener == null ? null
                : ((com.ixnah.mc.paperarc.bridge.ConnectionBridge) listener.connection).paper$getVirtualHost();
    }

    // ===== Audience 落地（paper-api CommandSender extends Audience，见 B4b）=====
    //
    // Audience 的方法体全是空 default：接口合并到运行时 CommandSender 之后，
    // 不覆盖这些"终端方法"消息就会被静默丢弃。终端方法由 javap 反汇编 adventure
    // 4.17.0 的 Audience 确定（其余重载都转调它们）：
    //   sendMessage(Identity, Component, MessageType) / sendActionBar(Component)
    //   sendPlayerListHeaderAndFooter(Component, Component) / sendTitlePart(TitlePart, T)
    //   clearTitle() / resetTitle() / playSound(...)×3 / stopSound(SoundStop)
    // 未落地（保持 adventure 的空 default，行为=无操作），原因同 1.20.1 分支：
    //   showBossBar/hideBossBar —— 需要把 adventure BossBar 的增量变更（名称/进度/
    //     颜色/overlay/flags）桥到 ServerBossEvent 并做监听器登记，超出本阶段范围；
    //     activeBossBars() 已按同样理由返回空集合。
    //   openBook —— Paper 靠临时替换主手物品下发 ClientboundOpenBookPacket，
    //     会与插件的背包状态互相干扰。
    //   deleteMessage —— 需要 MessageSignature 缓存，Arclight 无。

    @Unique
    public Component name() {
        return Component.text(getHandle().getGameProfile().getName());
    }

    @Unique
    public void sendMessage(net.kyori.adventure.identity.Identity source, Component message,
            net.kyori.adventure.audience.MessageType type) {
        net.minecraft.network.chat.Component vanilla = paperarc$vanilla(message);
        if (vanilla != null) {
            // 1.19+ 的未签名消息一律走系统聊天包，Paper 同样如此；overlay=false 表示聊天框
            paperarc$send(new net.minecraft.network.protocol.game.ClientboundSystemChatPacket(vanilla, false));
        }
    }

    @Unique
    public void sendActionBar(Component message) {
        net.minecraft.network.chat.Component vanilla = paperarc$vanilla(message);
        if (vanilla != null) {
            paperarc$send(new ClientboundSetActionBarTextPacket(vanilla));
        }
    }

    @Unique
    public void sendPlayerListHeaderAndFooter(Component header, Component footer) {
        this.playerListHeader = paperarc$vanilla(header);
        this.playerListFooter = paperarc$vanilla(footer);
        updatePlayerListHeaderFooter();
    }

    @Unique
    @SuppressWarnings("unchecked")
    public <T> void sendTitlePart(net.kyori.adventure.title.TitlePart<T> part, T value) {
        Preconditions.checkNotNull(part, "part");
        Preconditions.checkNotNull(value, "value");
        if (part == net.kyori.adventure.title.TitlePart.TITLE) {
            paperarc$send(new ClientboundSetTitleTextPacket(paperarc$vanilla((Component) value)));
        } else if (part == net.kyori.adventure.title.TitlePart.SUBTITLE) {
            paperarc$send(new ClientboundSetSubtitleTextPacket(paperarc$vanilla((Component) value)));
        } else if (part == net.kyori.adventure.title.TitlePart.TIMES) {
            net.kyori.adventure.title.Title.Times times = (net.kyori.adventure.title.Title.Times) value;
            paperarc$send(new ClientboundSetTitlesAnimationPacket(paperarc$ticks(times.fadeIn()),
                    paperarc$ticks(times.stay()), paperarc$ticks(times.fadeOut())));
        } else {
            throw new IllegalArgumentException("Unknown TitlePart " + part);
        }
    }

    @Unique
    private static int paperarc$ticks(java.time.Duration duration) {
        return duration == null ? 0 : (int) (duration.toMillis() / 50L);
    }

    @Unique
    public void clearTitle() {
        paperarc$send(new net.minecraft.network.protocol.game.ClientboundClearTitlesPacket(false));
    }

    @Unique
    public void resetTitle() {
        paperarc$send(new net.minecraft.network.protocol.game.ClientboundClearTitlesPacket(true));
    }

    @Unique
    public void playSound(net.kyori.adventure.sound.Sound sound) {
        ServerPlayer handle = getHandle();
        paperarc$playSound(sound, handle.getX(), handle.getY(), handle.getZ());
    }

    @Unique
    public void playSound(net.kyori.adventure.sound.Sound sound, double x, double y, double z) {
        paperarc$playSound(sound, x, y, z);
    }

    @Unique
    public void playSound(net.kyori.adventure.sound.Sound sound, net.kyori.adventure.sound.Sound.Emitter emitter) {
        if (sound == null || emitter == null) {
            return;
        }
        net.minecraft.world.entity.Entity target;
        if (emitter == net.kyori.adventure.sound.Sound.Emitter.self()) {
            target = getHandle();
        } else if (emitter instanceof CraftEntity craft) {
            target = craft.getHandle();
        } else {
            throw new IllegalArgumentException("Unknown Sound.Emitter " + emitter);
        }
        paperarc$playSound(sound, target.getX(), target.getY(), target.getZ());
    }

    /**
     * adventure 的 {@code Sound.name()} 是任意 {@code Key}（允许资源包自定义音效，
     * 不一定在 {@code BuiltInRegistries.SOUND_EVENT} 里），因此用
     * {@code Holder.direct} 直接下发，与 Paper 的做法一致。
     * 1.21.1 的 {@code ResourceLocation} 构造器已私有，改走 {@code fromNamespaceAndPath}。
     */
    @Unique
    private void paperarc$playSound(net.kyori.adventure.sound.Sound sound, double x, double y, double z) {
        if (sound == null) {
            return;
        }
        net.minecraft.resources.ResourceLocation id =
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                        sound.name().namespace(), sound.name().value());
        long seed = sound.seed().isPresent() ? sound.seed().getAsLong() : getHandle().getRandom().nextLong();
        paperarc$send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                net.minecraft.core.Holder.direct(net.minecraft.sounds.SoundEvent.createVariableRangeEvent(id)),
                paperarc$soundSource(sound.source()), x, y, z, sound.volume(), sound.pitch(), seed));
    }

    @Unique
    public void stopSound(net.kyori.adventure.sound.SoundStop stop) {
        if (stop == null) {
            return;
        }
        net.kyori.adventure.key.Key key = stop.sound();
        paperarc$send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(
                key == null ? null
                        : net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(key.namespace(), key.value()),
                paperarc$soundSource(stop.source())));
    }

    /** adventure {@code Sound.Source} 与 NMS {@code SoundSource} 的常量顺序完全一致（javap 核对）。 */
    @Unique
    private static net.minecraft.sounds.SoundSource paperarc$soundSource(
            net.kyori.adventure.sound.Sound.Source source) {
        return source == null ? null : net.minecraft.sounds.SoundSource.values()[source.ordinal()];
    }

    // ===== B2-4：IfaceMixin 早就声明、却一直没有实现体的 paper 方法（插件调用即 AbstractMethodError） =====

    @Unique
    public void kick() {
        this.kick(net.kyori.adventure.text.Component.translatable("multiplayer.disconnect.kicked"));
    }

    @Unique
    public void kick(net.kyori.adventure.text.Component message) {
        this.kick(message, org.bukkit.event.player.PlayerKickEvent.Cause.PLUGIN);
    }

    @Unique
    public void kick(net.kyori.adventure.text.Component message, org.bukkit.event.player.PlayerKickEvent.Cause cause) {
        // Arclight 的 PlayerKickEvent 没有 Cause 形参（那是 Paper 加的），拿不到透传口子，
        // 所以 cause 只影响我们这边的判断、不进事件对象；与 1.20.1 A1 的处理一致。
        ServerPlayer handle = getHandle();
        if (handle.connection == null) {
            return;
        }
        handle.connection.disconnect(message == null
                ? net.minecraft.network.chat.Component.empty()
                : paperarc$vanilla(message));
    }

    @Unique
    public java.util.Locale locale() {
        String tag = getHandle().clientInformation().language();
        if (tag == null || tag.isEmpty()) {
            return java.util.Locale.US;
        }
        // vanilla 用 "zh_cn" 这种下划线写法，Locale.forLanguageTag 要连字符
        return java.util.Locale.forLanguageTag(tag.replace('_', '-'));
    }

    @Unique
    public void hideTitle() {
        paperarc$send(new net.minecraft.network.protocol.game.ClientboundClearTitlesPacket(false));
    }

    @Unique
    public void lookAt(double x, double y, double z, io.papermc.paper.entity.LookAnchor playerAnchor) {
        Preconditions.checkNotNull(playerAnchor, "playerAnchor cannot be null");
        getHandle().lookAt(paperarc$anchor(playerAnchor), new net.minecraft.world.phys.Vec3(x, y, z));
    }

    @Unique
    public void lookAt(org.bukkit.entity.Entity entity, io.papermc.paper.entity.LookAnchor playerAnchor,
                       io.papermc.paper.entity.LookAnchor entityAnchor) {
        Preconditions.checkNotNull(entity, "entity cannot be null");
        Preconditions.checkNotNull(playerAnchor, "playerAnchor cannot be null");
        Preconditions.checkNotNull(entityAnchor, "entityAnchor cannot be null");
        getHandle().lookAt(paperarc$anchor(playerAnchor),
                ((CraftEntity) entity).getHandle(), paperarc$anchor(entityAnchor));
    }

    @Unique
    private static net.minecraft.commands.arguments.EntityAnchorArgument.Anchor paperarc$anchor(
            io.papermc.paper.entity.LookAnchor anchor) {
        return anchor == io.papermc.paper.entity.LookAnchor.EYES
                ? net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES
                : net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.FEET;
    }

    @Unique
    public void giveExp(int exp, boolean applyMending) {
        if (applyMending) {
            // vanilla giveExperiencePoints 里没有修补附魔那一步，Paper 把它拆成了
            // applyMending(amount) -> 剩余经验再进玩家。这里复刻同样的顺序。
            exp = ((org.bukkit.entity.Player) (Object) this).applyMending(exp);
        }
        getHandle().giveExperiencePoints(exp);
    }

    @Unique
    public boolean isListed(org.bukkit.entity.Player player) {
        Preconditions.checkArgument(player != null, "player must not be null");
        return !this.paperarc$unlisted.contains(player.getUniqueId());
    }

    @Unique
    public boolean listPlayer(org.bukkit.entity.Player player) {
        Preconditions.checkArgument(player != null, "player must not be null");
        if (!(player instanceof CraftPlayer) || getHandle().connection == null) {
            return false;
        }
        if (!this.paperarc$unlisted.remove(player.getUniqueId())) {
            return false;
        }
        ServerPlayer other = ((CraftPlayer) player).getHandle();
        paperarc$send(new ClientboundPlayerInfoUpdatePacket(
                EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED), List.of(other)));
        return true;
    }

    @Unique
    public net.kyori.adventure.text.Component playerListFooter() {
        return paperarc$adventure(playerListFooter);
    }

    @Unique
    public int getWardenWarningCooldown() {
        net.minecraft.world.entity.monster.warden.WardenSpawnTracker tracker = paperarc$wardenTracker();
        return tracker == null ? 0 : tracker.cooldownTicks;
    }

    @Unique
    public int getWardenTimeSinceLastWarning() {
        net.minecraft.world.entity.monster.warden.WardenSpawnTracker tracker = paperarc$wardenTracker();
        return tracker == null ? 0 : tracker.ticksSinceLastWarning;
    }

    @Unique
    public int getWardenWarningLevel() {
        net.minecraft.world.entity.monster.warden.WardenSpawnTracker tracker = paperarc$wardenTracker();
        return tracker == null ? 0 : tracker.warningLevel;
    }

    @Unique
    public void increaseWardenWarningLevel() {
        net.minecraft.world.entity.monster.warden.WardenSpawnTracker tracker = paperarc$wardenTracker();
        if (tracker != null) {
            tracker.setWarningLevel(tracker.getWarningLevel() + 1);
        }
    }

    @Unique
    public int getViewDistance() {
        return getHandle().requestedViewDistance();
    }

    @Unique
    public boolean hasSeenWinScreen() {
        return getHandle().seenCredits;
    }

    @Unique
    public TriState hasFlyingFallDamage() {
        return this.paperarc$flyingFallDamage == null ? TriState.NOT_SET : this.paperarc$flyingFallDamage;
    }

    @Unique
    public boolean isChunkSent(long chunkKey) {
        ServerPlayer handle = getHandle();
        if (handle.connection == null) {
            return false;
        }
        // chunkKey 的低 32 位是 x、高 32 位是 z（与 Chunk.getChunkKey 一致）
        return handle.serverLevel().getChunkSource().chunkMap.isChunkTracked(
                handle, (int) chunkKey, (int) (chunkKey >> 32));
    }
}
