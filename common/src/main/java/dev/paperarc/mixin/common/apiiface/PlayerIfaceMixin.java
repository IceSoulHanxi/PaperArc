package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Player} (generated).
 * Adds 69 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.entity.Player", remap = false)
public interface PlayerIfaceMixin {

    public abstract java.lang.Iterable activeBossBars();

    public abstract net.kyori.adventure.text.Component displayName();

    public abstract void displayName(net.kyori.adventure.text.Component p0);

    public abstract void playerListName(net.kyori.adventure.text.Component p0);

    public abstract net.kyori.adventure.text.Component playerListName();

    public abstract net.kyori.adventure.text.Component playerListHeader();

    public abstract net.kyori.adventure.text.Component playerListFooter();

    public abstract void setPlayerListHeaderFooter(net.md_5.bungee.api.chat.BaseComponent p0, net.md_5.bungee.api.chat.BaseComponent p1);

    public abstract java.net.InetSocketAddress getHAProxyAddress();

    public abstract void kick();

    public abstract void kick(net.kyori.adventure.text.Component p0);

    public abstract void kick(net.kyori.adventure.text.Component p0, org.bukkit.event.player.PlayerKickEvent.Cause p1);

    public abstract void showWinScreen();

    public abstract boolean hasSeenWinScreen();

    public abstract void setHasSeenWinScreen(boolean p0);

    public abstract void sendActionBar(java.lang.String p0);

    public abstract void sendActionBar(net.md_5.bungee.api.chat.BaseComponent[] p0);

    public abstract void setTitleTimes(int p0, int p1, int p2);

    public abstract void setSubtitle(net.md_5.bungee.api.chat.BaseComponent p0);

    public abstract void showTitle(net.md_5.bungee.api.chat.BaseComponent p0);

    public abstract void showTitle(net.md_5.bungee.api.chat.BaseComponent p0, net.md_5.bungee.api.chat.BaseComponent p1, int p2, int p3, int p4);

    public abstract void sendTitle(com.destroystokyo.paper.Title p0);

    public abstract void updateTitle(com.destroystokyo.paper.Title p0);

    public abstract void hideTitle();

    public abstract void giveExp(int p0, boolean p1);

    public abstract int applyMending(int p0);

    public abstract int calculateTotalExperiencePoints();

    public abstract void setExperienceLevelAndProgress(int p0);

    public abstract int getExperiencePointsNeededForNextLevel();

    public abstract void setFlyingFallDamage(net.kyori.adventure.util.TriState p0);

    public abstract net.kyori.adventure.util.TriState hasFlyingFallDamage();

    public abstract boolean isListed(org.bukkit.entity.Player p0);

    public abstract boolean unlistPlayer(org.bukkit.entity.Player p0);

    public abstract boolean listPlayer(org.bukkit.entity.Player p0);

    public abstract org.bukkit.event.player.PlayerResourcePackStatusEvent.Status getResourcePackStatus();

    public abstract java.util.Locale locale();

    public abstract boolean getAffectsSpawning();

    public abstract void setAffectsSpawning(boolean p0);

    public abstract int getViewDistance();

    public abstract void setViewDistance(int p0);

    public abstract int getSimulationDistance();

    public abstract void setSimulationDistance(int p0);

    public abstract int getSendViewDistance();

    public abstract void setSendViewDistance(int p0);

    public abstract void setPlayerProfile(com.destroystokyo.paper.profile.PlayerProfile p0);

    public abstract float getCooldownPeriod();

    public abstract float getCooledAttackStrength(float p0);

    public abstract void resetCooldown();

    public abstract java.lang.Object getClientOption(com.destroystokyo.paper.ClientOption p0);

    public abstract void sendOpLevel(byte p0);

    public abstract void addAdditionalChatCompletions(java.util.Collection p0);

    public abstract void removeAdditionalChatCompletions(java.util.Collection p0);

    public abstract java.lang.String getClientBrandName();

    public abstract void lookAt(double p0, double p1, double p2, io.papermc.paper.entity.LookAnchor p3);

    public abstract void lookAt(org.bukkit.entity.Entity p0, io.papermc.paper.entity.LookAnchor p1, io.papermc.paper.entity.LookAnchor p2);

    public abstract void showElderGuardian(boolean p0);

    public abstract int getWardenWarningCooldown();

    public abstract void setWardenWarningCooldown(int p0);

    public abstract int getWardenTimeSinceLastWarning();

    public abstract void setWardenTimeSinceLastWarning(int p0);

    public abstract int getWardenWarningLevel();

    public abstract void setWardenWarningLevel(int p0);

    public abstract void increaseWardenWarningLevel();

    public abstract java.time.Duration getIdleDuration();

    public abstract void resetIdleDuration();

    public abstract java.util.Set getSentChunkKeys();

    public abstract java.util.Set getSentChunks();

    public abstract boolean isChunkSent(long p0);

    public abstract void sendEntityEffect(org.bukkit.EntityEffect p0, org.bukkit.entity.Entity p1);
}
