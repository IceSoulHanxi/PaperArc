package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import com.destroystokyo.paper.ClientOption;
import com.destroystokyo.paper.Title;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.base.Preconditions;
import io.papermc.paper.entity.LookAnchor;
import io.papermc.paper.math.Position;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackInfoLike;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEvent.ShowEntity;
import net.kyori.adventure.util.TriState;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.GameMode;
import org.bukkit.Instrument;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Note;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.ServerLinks;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.WeatherType;
import org.bukkit.WorldBorder;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.sign.Side;
import org.bukkit.conversations.Conversable;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageRecipient;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import com.destroystokyo.paper.network.NetworkClient;
import net.kyori.adventure.bossbar.BossBarViewer;
import net.kyori.adventure.identity.Identified;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Player} (generated).
 * Adds 69 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 *
 * <p>paper-api 的 {@code Player extends Identified, BossBarViewer, NetworkClient}；
 * Mixin 会把 mixin 自身的父接口合并到接口目标上（B4b）。实现体在
 * {@code CraftPlayerApiMixin}：{@code identity()}、{@code getProtocolVersion()}、
 * {@code getVirtualHost()}；{@code activeBossBars()} 早已有（返回空集合，理由见那里）。
 */
@Mixin(targets = "org.bukkit.entity.Player", remap = false)
public interface PlayerIfaceMixin extends Identified, BossBarViewer, NetworkClient {

    @Unique
    public abstract java.lang.Iterable activeBossBars();

    @Unique
    public abstract net.kyori.adventure.text.Component displayName();

    @Unique
    public abstract void displayName(net.kyori.adventure.text.Component p0);

    @Unique
    public abstract void playerListName(net.kyori.adventure.text.Component p0);

    @Unique
    public abstract net.kyori.adventure.text.Component playerListName();

    @Unique
    public abstract net.kyori.adventure.text.Component playerListHeader();

    @Unique
    public abstract net.kyori.adventure.text.Component playerListFooter();

    @Unique
    public abstract void setPlayerListHeaderFooter(net.md_5.bungee.api.chat.BaseComponent p0, net.md_5.bungee.api.chat.BaseComponent p1);

    @Unique
    public abstract java.net.InetSocketAddress getHAProxyAddress();

    @Unique
    public abstract void kick();

    @Unique
    public abstract void kick(net.kyori.adventure.text.Component p0);

    @Unique
    public abstract void showWinScreen();

    @Unique
    public abstract boolean hasSeenWinScreen();

    @Unique
    public abstract void setHasSeenWinScreen(boolean p0);

    @Unique
    public abstract void sendActionBar(java.lang.String p0);

    @Unique
    public abstract void sendActionBar(char p0, java.lang.String p1);

    @Unique
    public abstract void setTitleTimes(int p0, int p1, int p2);

    @Unique
    public abstract void setSubtitle(net.md_5.bungee.api.chat.BaseComponent p0);

    @Unique
    public abstract void showTitle(net.md_5.bungee.api.chat.BaseComponent p0);

    @Unique
    public abstract void showTitle(net.md_5.bungee.api.chat.BaseComponent p0, net.md_5.bungee.api.chat.BaseComponent p1, int p2, int p3, int p4);

    @Unique
    public abstract void sendTitle(com.destroystokyo.paper.Title p0);

    @Unique
    public abstract void updateTitle(com.destroystokyo.paper.Title p0);

    @Unique
    public abstract void hideTitle();

    @Unique
    public abstract void giveExp(int p0, boolean p1);

    @Unique
    public abstract int applyMending(int p0);

    @Unique
    public abstract int calculateTotalExperiencePoints();

    @Unique
    public abstract void setExperienceLevelAndProgress(int p0);

    @Unique
    public abstract int getExperiencePointsNeededForNextLevel();

    @Unique
    public abstract void setFlyingFallDamage(net.kyori.adventure.util.TriState p0);

    @Unique
    public abstract net.kyori.adventure.util.TriState hasFlyingFallDamage();

    @Unique
    public abstract boolean isListed(org.bukkit.entity.Player p0);

    @Unique
    public abstract boolean unlistPlayer(org.bukkit.entity.Player p0);

    @Unique
    public abstract boolean listPlayer(org.bukkit.entity.Player p0);

    @Unique
    public abstract org.bukkit.event.player.PlayerResourcePackStatusEvent.Status getResourcePackStatus();

    @Unique
    public abstract java.util.Locale locale();

    @Unique
    public abstract boolean getAffectsSpawning();

    @Unique
    public abstract void setAffectsSpawning(boolean p0);

    @Unique
    public abstract int getViewDistance();

    @Unique
    public abstract void setViewDistance(int p0);

    @Unique
    public abstract int getSimulationDistance();

    @Unique
    public abstract void setSimulationDistance(int p0);

    @Unique
    public abstract int getSendViewDistance();

    @Unique
    public abstract void setSendViewDistance(int p0);

    @Unique
    public abstract void setPlayerProfile(com.destroystokyo.paper.profile.PlayerProfile p0);

    @Unique
    public abstract float getCooldownPeriod();

    @Unique
    public abstract float getCooledAttackStrength(float p0);

    @Unique
    public abstract void resetCooldown();

    @Unique
    public abstract java.lang.Object getClientOption(com.destroystokyo.paper.ClientOption p0);

    @Unique
    public abstract void sendOpLevel(byte p0);

    @Unique
    public abstract void addAdditionalChatCompletions(java.util.Collection p0);

    @Unique
    public abstract void removeAdditionalChatCompletions(java.util.Collection p0);

    @Unique
    public abstract java.lang.String getClientBrandName();

    @Unique
    public abstract void lookAt(double p0, double p1, double p2, io.papermc.paper.entity.LookAnchor p3);

    @Unique
    public abstract void lookAt(org.bukkit.entity.Entity p0, io.papermc.paper.entity.LookAnchor p1, io.papermc.paper.entity.LookAnchor p2);

    @Unique
    public abstract void showElderGuardian(boolean p0);

    @Unique
    public abstract int getWardenWarningCooldown();

    @Unique
    public abstract void setWardenWarningCooldown(int p0);

    @Unique
    public abstract int getWardenTimeSinceLastWarning();

    @Unique
    public abstract void setWardenTimeSinceLastWarning(int p0);

    @Unique
    public abstract int getWardenWarningLevel();

    @Unique
    public abstract void setWardenWarningLevel(int p0);

    @Unique
    public abstract void increaseWardenWarningLevel();

    @Unique
    public abstract java.time.Duration getIdleDuration();

    @Unique
    public abstract void resetIdleDuration();

    @Unique
    public abstract java.util.Set getSentChunkKeys();

    @Unique
    public abstract java.util.Set getSentChunks();

    @Unique
    public abstract boolean isChunkSent(long p0);

    @Unique
    public abstract void sendEntityEffect(org.bukkit.EntityEffect p0, org.bukkit.entity.Entity p1);

    @Unique
    public abstract void sendActionBar(net.md_5.bungee.api.chat.BaseComponent[] p0);

    @Unique
    public abstract void sendMultiBlockChange(java.util.Map p0);

    @Unique
    public abstract void sendSignChange(org.bukkit.Location p0, java.util.List p1, org.bukkit.DyeColor p2, boolean p3);

    @Unique
    public abstract void setPlayerListHeaderFooter(net.md_5.bungee.api.chat.BaseComponent[] p0, net.md_5.bungee.api.chat.BaseComponent[] p1);

    @Unique
    public abstract void setResourcePack(java.util.UUID p0, java.lang.String p1, byte[] p2, net.kyori.adventure.text.Component p3, boolean p4);

    @Unique
    public abstract void setSubtitle(net.md_5.bungee.api.chat.BaseComponent[] p0);

    @Unique
    public abstract void showTitle(net.md_5.bungee.api.chat.BaseComponent[] p0);

    @Unique
    public abstract void showTitle(net.md_5.bungee.api.chat.BaseComponent[] p0, net.md_5.bungee.api.chat.BaseComponent[] p1, int p2, int p3, int p4);

    @Unique
    public abstract com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile();

    @Unique
    public default Identity identity() {
        Player self = (Player) this;
        return Identity.identity(self.getUniqueId());
    }

    @Unique
    public default void sendMultiBlockChange(Map<? extends Position, BlockData> blockChanges, boolean suppressLightUpdates) {
        Player self = (Player) this;
        self.sendMultiBlockChange(blockChanges);
    }

    @Unique
    public default void sendSignChange(Location loc, List<? extends Component> lines) throws IllegalArgumentException {
        Player self = (Player) this;
        self.sendSignChange(loc, lines, DyeColor.BLACK);
    }

    @Unique
    public default void sendSignChange(Location loc, List<? extends Component> lines, DyeColor dyeColor) throws IllegalArgumentException {
        Player self = (Player) this;
        self.sendSignChange(loc, lines, dyeColor, false);
    }

    @Unique
    public default void sendSignChange(Location loc, List<? extends Component> lines, boolean hasGlowingText) throws IllegalArgumentException {
        Player self = (Player) this;
        self.sendSignChange(loc, lines, DyeColor.BLACK, hasGlowingText);
    }

    @Unique
    public default BanEntry banPlayerFull(String reason) {
        Player self = (Player) this;
        return self.banPlayerFull(reason, (Date) null, (String) null);
    }

    @Unique
    public default BanEntry banPlayerFull(String reason, String source) {
        Player self = (Player) this;
        return self.banPlayerFull(reason, (Date) null, source);
    }

    @Unique
    public default BanEntry banPlayerFull(String reason, Date expires) {
        Player self = (Player) this;
        return self.banPlayerFull(reason, expires, (String) null);
    }

    @Unique
    public default BanEntry banPlayerFull(String reason, Date expires, String source) {
        Player self = (Player) this;
        self.banPlayer(reason, expires, source);
        return self.banPlayerIP(reason, expires, source, true);
    }

    @Unique
    public default BanEntry banPlayerIP(String reason, boolean kickPlayer) {
        Player self = (Player) this;
        return self.banPlayerIP(reason, (Date) null, (String) null, kickPlayer);
    }

    @Unique
    public default BanEntry banPlayerIP(String reason, String source, boolean kickPlayer) {
        Player self = (Player) this;
        return self.banPlayerIP(reason, (Date) null, source, kickPlayer);
    }

    @Unique
    public default BanEntry banPlayerIP(String reason, Date expires, boolean kickPlayer) {
        Player self = (Player) this;
        return self.banPlayerIP(reason, expires, (String) null, kickPlayer);
    }

    @Unique
    public default BanEntry banPlayerIP(String reason) {
        Player self = (Player) this;
        return self.banPlayerIP(reason, (Date) null, (String) null);
    }

    @Unique
    public default BanEntry banPlayerIP(String reason, String source) {
        Player self = (Player) this;
        return self.banPlayerIP(reason, (Date) null, source);
    }

    @Unique
    public default BanEntry banPlayerIP(String reason, Date expires) {
        Player self = (Player) this;
        return self.banPlayerIP(reason, expires, (String) null);
    }

    @Unique
    public default BanEntry banPlayerIP(String reason, Date expires, String source) {
        Player self = (Player) this;
        return self.banPlayerIP(reason, expires, source, true);
    }

    @Unique
    public default BanEntry banPlayerIP(String reason, Date expires, String source, boolean kickPlayer) {
        Player self = (Player) this;
        BanEntry banEntry = Bukkit.getServer().getBanList(BanList.Type.IP).addBan(self.getAddress().getAddress().getHostAddress(), reason, expires, source);

        if (kickPlayer && self.isOnline()) {
            self.getPlayer().kickPlayer(reason);
        }

        return banEntry;
    }

    @Unique
    public default void sendMessage(BaseComponent component) {
        Player self = (Player) this;
        self.spigot().sendMessage(component);
    }

    @Unique
    public default void sendMessage(BaseComponent... components) {
        Player self = (Player) this;
        self.spigot().sendMessage(components);
    }

    @Unique
    public default void sendMessage(ChatMessageType position, BaseComponent... components) {
        Player self = (Player) this;
        self.spigot().sendMessage(position, components);
    }

    @Unique
    public default void setResourcePack(String url, byte [] hash, Component prompt) {
        Player self = (Player) this;
        self.setResourcePack(url, hash, prompt, false);
    }

    @Unique
    public default void setResourcePack(String url, byte [] hash, Component prompt, boolean force) {
        Player self = (Player) this;
        self.setResourcePack(UUID.nameUUIDFromBytes(url.getBytes(StandardCharsets.UTF_8)), url, hash, prompt, force);
    }

    @Unique
    public default void setResourcePack(String url, String hash) {
        Player self = (Player) this;
        self.setResourcePack(url, hash, false);
    }

    @Unique
    public default void setResourcePack(String url, String hash, boolean required) {
        Player self = (Player) this;
        self.setResourcePack(url, hash, required, (Component) null);
    }

    @Unique
    public default void setResourcePack(String url, String hash, boolean required, Component resourcePackPrompt) {
        Player self = (Player) this;
        self.setResourcePack(UUID.nameUUIDFromBytes(url.getBytes(StandardCharsets.UTF_8)), url, hash, resourcePackPrompt, required);
    }

    @Unique
    public default void setResourcePack(UUID uuid, String url, String hash, Component resourcePackPrompt, boolean required) {
        Player self = (Player) this;
        self.sendResourcePacks(ResourcePackRequest.resourcePackRequest().required(required).replace(true).prompt(resourcePackPrompt).packs(ResourcePackInfo.resourcePackInfo(uuid, URI.create(url), hash), new ResourcePackInfoLike[0]));
    }

    @Unique
    public default String getResourcePackHash() {
        return null;
    }

    @Unique
    public default boolean hasResourcePack() {
        Player self = (Player) this;
        return self.getResourcePackStatus() == PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED;
    }

    @Unique
    public default int getNoTickViewDistance() {
        Player self = (Player) this;
        return self.getViewDistance();
    }

    @Unique
    public default void setNoTickViewDistance(int viewDistance) {
        Player self = (Player) this;
        self.setViewDistance(viewDistance);
    }

    @Unique
    public default Firework boostElytra(ItemStack firework) {
        Player self = (Player) this;
        Preconditions.checkState(self.isGliding(), "Player must be gliding");
        return self.fireworkBoost(firework);
    }

    @Unique
    public default void lookAt(Position position, LookAnchor playerAnchor) {
        Player self = (Player) this;
        self.lookAt(position.x(), position.y(), position.z(), playerAnchor);
    }

    @Unique
    public default void showElderGuardian() {
        Player self = (Player) this;
        self.showElderGuardian(false);
    }
}
