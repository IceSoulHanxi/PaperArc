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
}
