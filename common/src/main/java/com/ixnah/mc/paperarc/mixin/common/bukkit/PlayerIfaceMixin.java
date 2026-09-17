package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.entity.Player} (generated, trimmed for 1.20.1).
 * Adds 58 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 *
 * <p>paper-api 的 {@code Player} 还多三个父接口 —— {@code Identified}（{@code identity()}）、
 * {@code BossBarViewer}（{@code activeBossBars()}，已有实现体）与
 * {@code com.destroystokyo.paper.network.NetworkClient}（{@code getAddress}/
 * {@code getProtocolVersion}/{@code getVirtualHost}）。实现在 CraftPlayerApiMixin。
 */
@Mixin(targets = "org.bukkit.entity.Player", remap = false)
public interface PlayerIfaceMixin extends
        net.kyori.adventure.identity.Identified,
        net.kyori.adventure.bossbar.BossBarViewer,
        com.destroystokyo.paper.network.NetworkClient {

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
    public abstract void kick();

    @Unique
    public abstract void kick(net.kyori.adventure.text.Component p0);

    @Unique
    public abstract void kick(net.kyori.adventure.text.Component p0, org.bukkit.event.player.PlayerKickEvent.Cause p1);

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
    public abstract int getNoTickViewDistance();
    @Unique
    public abstract void setNoTickViewDistance(int viewDistance);
    @Unique
    public abstract java.lang.String getResourcePackHash();
    @Unique
    public abstract boolean hasResourcePack();
    @Unique
    public abstract com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile();
    @Unique
    public abstract org.bukkit.entity.Firework boostElytra(org.bukkit.inventory.ItemStack itemStack);
    @Unique
    public abstract void setRotation(float yaw, float pitch);
    @Unique
    public abstract void sendMultiBlockChange(java.util.Map<? extends io.papermc.paper.math.Position, org.bukkit.block.data.BlockData> p0);
    @Unique
    public abstract void setResourcePack(java.lang.String p0, byte[] p1, net.kyori.adventure.text.Component p2, boolean p3);
    @Unique
    public abstract void setResourcePack(java.lang.String p0, java.lang.String p1);
    @Unique
    public abstract void setResourcePack(java.lang.String p0, java.lang.String p1, boolean p2);
    @Unique
    public abstract void setResourcePack(java.lang.String p0, java.lang.String p1, boolean p2, net.kyori.adventure.text.Component p3);
    // ===== A4-4：paper-api 的 default 方法体照搬（运行时接口里一个都没有）=====

    @Unique
    public default net.kyori.adventure.identity.Identity identity() {
        return net.kyori.adventure.identity.Identity.identity(((org.bukkit.entity.Player) this).getUniqueId());
    }

    @Unique
    public default void showElderGuardian() {
        showElderGuardian(false);
    }

    @Unique
    public default void lookAt(io.papermc.paper.math.Position position, io.papermc.paper.entity.LookAnchor playerAnchor) {
        lookAt(position.x(), position.y(), position.z(), playerAnchor);
    }

    @Unique
    public default void setResourcePack(String url, byte[] hash, net.kyori.adventure.text.Component prompt) {
        setResourcePack(url, hash, prompt, false);
    }

    @Unique
    public default void sendMessage(net.md_5.bungee.api.chat.BaseComponent component) {
        ((org.bukkit.entity.Player) this).spigot().sendMessage(component);
    }

    @Unique
    public default void sendMessage(net.md_5.bungee.api.chat.BaseComponent... components) {
        ((org.bukkit.entity.Player) this).spigot().sendMessage(components);
    }

    @Unique
    public default void sendMessage(net.md_5.bungee.api.ChatMessageType position, net.md_5.bungee.api.chat.BaseComponent... components) {
        ((org.bukkit.entity.Player) this).spigot().sendMessage(position, components);
    }

    /** paper 的 Component 版换牌：转成 legacy 字符串走运行时已有的 String[] 重载。 */
    @Unique
    public default void sendSignChange(org.bukkit.Location loc, java.util.List<net.kyori.adventure.text.Component> lines,
            org.bukkit.DyeColor dyeColor, boolean hasGlowingText) {
        String[] legacy = new String[4];
        for (int i = 0; i < 4; i++) {
            net.kyori.adventure.text.Component line = lines == null || i >= lines.size() ? null : lines.get(i);
            legacy[i] = line == null ? ""
                    : net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(line);
        }
        ((org.bukkit.entity.Player) this).sendSignChange(loc, legacy, dyeColor, hasGlowingText);
    }

    @Unique
    public default void sendSignChange(org.bukkit.Location loc, java.util.List<net.kyori.adventure.text.Component> lines) {
        sendSignChange(loc, lines, org.bukkit.DyeColor.BLACK, false);
    }

    @Unique
    public default void sendSignChange(org.bukkit.Location loc, java.util.List<net.kyori.adventure.text.Component> lines,
            org.bukkit.DyeColor dyeColor) {
        sendSignChange(loc, lines, dyeColor, false);
    }

    @Unique
    public default void sendSignChange(org.bukkit.Location loc, java.util.List<net.kyori.adventure.text.Component> lines,
            boolean hasGlowingText) {
        sendSignChange(loc, lines, org.bukkit.DyeColor.BLACK, hasGlowingText);
    }

    @Unique
    public default org.bukkit.BanEntry banPlayerFull(String reason) {
        return banPlayerFull(reason, null, null);
    }

    @Unique
    public default org.bukkit.BanEntry banPlayerFull(String reason, String source) {
        return banPlayerFull(reason, null, source);
    }

    @Unique
    public default org.bukkit.BanEntry banPlayerFull(String reason, java.util.Date expires) {
        return banPlayerFull(reason, expires, null);
    }

    @Unique
    public default org.bukkit.BanEntry banPlayerFull(String reason, java.util.Date expires, String source) {
        ((org.bukkit.OfflinePlayer) this).banPlayer(reason, expires, source);
        return banPlayerIP(reason, expires, source, true);
    }

    @Unique
    public default org.bukkit.BanEntry banPlayerIP(String reason) {
        return banPlayerIP(reason, null, null);
    }

    @Unique
    public default org.bukkit.BanEntry banPlayerIP(String reason, boolean kickPlayer) {
        return banPlayerIP(reason, null, null, kickPlayer);
    }

    @Unique
    public default org.bukkit.BanEntry banPlayerIP(String reason, String source) {
        return banPlayerIP(reason, null, source);
    }

    @Unique
    public default org.bukkit.BanEntry banPlayerIP(String reason, String source, boolean kickPlayer) {
        return banPlayerIP(reason, null, source, kickPlayer);
    }

    @Unique
    public default org.bukkit.BanEntry banPlayerIP(String reason, java.util.Date expires) {
        return banPlayerIP(reason, expires, null);
    }

    @Unique
    public default org.bukkit.BanEntry banPlayerIP(String reason, java.util.Date expires, boolean kickPlayer) {
        return banPlayerIP(reason, expires, null, kickPlayer);
    }

    @Unique
    public default org.bukkit.BanEntry banPlayerIP(String reason, java.util.Date expires, String source) {
        return banPlayerIP(reason, expires, source, true);
    }

    @Unique
    public default org.bukkit.BanEntry banPlayerIP(String reason, java.util.Date expires, String source, boolean kickPlayer) {
        org.bukkit.entity.Player self = (org.bukkit.entity.Player) this;
        org.bukkit.BanEntry banEntry = org.bukkit.Bukkit.getServer()
                .getBanList(org.bukkit.BanList.Type.IP)
                .addBan(self.getAddress().getAddress().getHostAddress(), reason, expires, source);
        if (kickPlayer) {
            self.kickPlayer(reason);
        }
        return banEntry;
    }

    /** 实现体在 CraftEntityApiMixin 上（HoverEventSource 的终端方法）。 */
    @Unique
    public abstract net.kyori.adventure.text.event.HoverEvent<net.kyori.adventure.text.event.HoverEvent.ShowEntity> asHoverEvent(java.util.function.UnaryOperator<net.kyori.adventure.text.event.HoverEvent.ShowEntity> op);

    @Unique
    public default void sendMultiBlockChange(java.util.Map<? extends io.papermc.paper.math.Position, org.bukkit.block.data.BlockData> blockChanges, boolean suppressLightUpdates) {
        // Arclight 没有 Paper 的“抑制光照更新”通路，退化为普通多方块变更
        sendMultiBlockChange(blockChanges);
    }

}
