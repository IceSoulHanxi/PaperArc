package com.ixnah.mc.paperarc.bridge;

import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

/**
 * Paper 在各 vanilla 调用点直接把 Cause/Reason 当形参传进事件构造器；Arclight 的事件
 * 构造点只有一处、且签名是 spigot 的老版本，改不了形参。于是用 {@link LaunchState}
 * 那一套 ThreadLocal：**调用点压值 → 事件构造器取值**（取即清，取不到用 Paper 的默认值）。
 *
 * <p>压值的一律 try/finally 清掉，避免"这次没触发事件"把值留给下一次。
 * 只有 {@code setIfAbsent} 系列例外：外层调用点已经给出更精确的原因时，
 * 内层通用入口（如 {@code CraftHumanEntity#closeInventory()}）不覆盖它。
 */
public final class EventCauseState {

    private EventCauseState() {
    }

    private static final ThreadLocal<InventoryCloseEvent.Reason> INVENTORY_CLOSE = new ThreadLocal<>();
    private static final ThreadLocal<PlayerKickEvent.Cause> KICK = new ThreadLocal<>();
    private static final ThreadLocal<PlayerQuitEvent.QuitReason> QUIT = new ThreadLocal<>();
    private static final ThreadLocal<PlayerGameModeChangeEvent.Cause> GAME_MODE = new ThreadLocal<>();
    private static final ThreadLocal<WeatherChangeEvent.Cause> WEATHER = new ThreadLocal<>();
    private static final ThreadLocal<ThunderChangeEvent.Cause> THUNDER = new ThreadLocal<>();

    // ---- InventoryCloseEvent.Reason ----

    public static void setInventoryCloseReason(InventoryCloseEvent.Reason reason) {
        INVENTORY_CLOSE.set(reason);
    }

    public static void setInventoryCloseReasonIfAbsent(InventoryCloseEvent.Reason reason) {
        if (INVENTORY_CLOSE.get() == null) {
            INVENTORY_CLOSE.set(reason);
        }
    }

    public static InventoryCloseEvent.Reason takeInventoryCloseReason() {
        InventoryCloseEvent.Reason reason = INVENTORY_CLOSE.get();
        INVENTORY_CLOSE.remove();
        return reason == null ? InventoryCloseEvent.Reason.UNKNOWN : reason;
    }

    public static void clearInventoryCloseReason() {
        INVENTORY_CLOSE.remove();
    }

    // ---- PlayerKickEvent.Cause ----

    public static void setKickCause(PlayerKickEvent.Cause cause) {
        KICK.set(cause);
    }

    public static PlayerKickEvent.Cause takeKickCause() {
        PlayerKickEvent.Cause cause = KICK.get();
        KICK.remove();
        return cause == null ? PlayerKickEvent.Cause.UNKNOWN : cause;
    }

    public static void clearKickCause() {
        KICK.remove();
    }

    // ---- PlayerQuitEvent.QuitReason ----

    public static void setQuitReason(PlayerQuitEvent.QuitReason reason) {
        QUIT.set(reason);
    }

    public static void setQuitReasonIfAbsent(PlayerQuitEvent.QuitReason reason) {
        if (QUIT.get() == null) {
            QUIT.set(reason);
        }
    }

    public static PlayerQuitEvent.QuitReason takeQuitReason() {
        PlayerQuitEvent.QuitReason reason = QUIT.get();
        QUIT.remove();
        return reason == null ? PlayerQuitEvent.QuitReason.DISCONNECTED : reason;
    }

    public static void clearQuitReason() {
        QUIT.remove();
    }

    // ---- PlayerGameModeChangeEvent.Cause ----

    public static void setGameModeCause(PlayerGameModeChangeEvent.Cause cause) {
        GAME_MODE.set(cause);
    }

    public static PlayerGameModeChangeEvent.Cause takeGameModeCause() {
        PlayerGameModeChangeEvent.Cause cause = GAME_MODE.get();
        GAME_MODE.remove();
        return cause == null ? PlayerGameModeChangeEvent.Cause.UNKNOWN : cause;
    }

    public static void clearGameModeCause() {
        GAME_MODE.remove();
    }

    /**
     * 最近一次在本线程构造出来的 {@code PlayerGameModeChangeEvent}。
     * {@code /gamemode} 需要在 {@code setGameMode} 返回 false 之后拿到事件的
     * {@code cancelMessage()} 发给命令发送者（Paper 的做法是直接返回事件对象，
     * Arclight 的 {@code changeGameModeForPlayer} 只返回 boolean）。
     */
    private static final ThreadLocal<PlayerGameModeChangeEvent> LAST_GAME_MODE_EVENT = new ThreadLocal<>();

    public static void setLastGameModeEvent(PlayerGameModeChangeEvent event) {
        LAST_GAME_MODE_EVENT.set(event);
    }

    public static PlayerGameModeChangeEvent takeLastGameModeEvent() {
        PlayerGameModeChangeEvent event = LAST_GAME_MODE_EVENT.get();
        LAST_GAME_MODE_EVENT.remove();
        return event;
    }

    public static void clearLastGameModeEvent() {
        LAST_GAME_MODE_EVENT.remove();
    }

    // ---- Weather / Thunder ----

    public static void setWeatherCause(WeatherChangeEvent.Cause cause) {
        WEATHER.set(cause);
    }

    public static WeatherChangeEvent.Cause takeWeatherCause() {
        WeatherChangeEvent.Cause cause = WEATHER.get();
        WEATHER.remove();
        return cause == null ? WeatherChangeEvent.Cause.NATURAL : cause;
    }

    public static void clearWeatherCause() {
        WEATHER.remove();
    }

    public static void setThunderCause(ThunderChangeEvent.Cause cause) {
        THUNDER.set(cause);
    }

    public static ThunderChangeEvent.Cause takeThunderCause() {
        ThunderChangeEvent.Cause cause = THUNDER.get();
        THUNDER.remove();
        return cause == null ? ThunderChangeEvent.Cause.NATURAL : cause;
    }

    public static void clearThunderCause() {
        THUNDER.remove();
    }

    // ---- 触发点上下文（批 3）----

    private static final ThreadLocal<org.bukkit.block.BlockFace> BLOCK_DAMAGE_FACE = new ThreadLocal<>();
    private static final ThreadLocal<org.bukkit.entity.Entity> EXP_SOURCE = new ThreadLocal<>();

    public static void setBlockDamageFace(org.bukkit.block.BlockFace face) {
        BLOCK_DAMAGE_FACE.set(face);
    }

    public static org.bukkit.block.BlockFace takeBlockDamageFace() {
        org.bukkit.block.BlockFace face = BLOCK_DAMAGE_FACE.get();
        BLOCK_DAMAGE_FACE.remove();
        return face;
    }

    public static void clearBlockDamageFace() {
        BLOCK_DAMAGE_FACE.remove();
    }

    public static void setExperienceSource(org.bukkit.entity.Entity source) {
        EXP_SOURCE.set(source);
    }

    public static org.bukkit.entity.Entity takeExperienceSource() {
        org.bukkit.entity.Entity source = EXP_SOURCE.get();
        EXP_SOURCE.remove();
        return source;
    }

    public static void clearExperienceSource() {
        EXP_SOURCE.remove();
    }

    // ---- AsyncPlayerPreLoginEvent 的三样上下文（批 3）----

    private static final ThreadLocal<String> PRE_LOGIN_HOSTNAME = new ThreadLocal<>();
    private static final ThreadLocal<java.net.InetAddress> PRE_LOGIN_RAW_ADDRESS = new ThreadLocal<>();
    private static final ThreadLocal<com.destroystokyo.paper.profile.PlayerProfile> PRE_LOGIN_PROFILE =
            new ThreadLocal<>();

    public static void setPreLoginContext(String hostname, java.net.InetAddress rawAddress,
                                          com.destroystokyo.paper.profile.PlayerProfile profile) {
        PRE_LOGIN_HOSTNAME.set(hostname);
        PRE_LOGIN_RAW_ADDRESS.set(rawAddress);
        PRE_LOGIN_PROFILE.set(profile);
    }

    public static String takePreLoginHostname() {
        String hostname = PRE_LOGIN_HOSTNAME.get();
        PRE_LOGIN_HOSTNAME.remove();
        return hostname;
    }

    public static java.net.InetAddress takePreLoginRawAddress() {
        java.net.InetAddress address = PRE_LOGIN_RAW_ADDRESS.get();
        PRE_LOGIN_RAW_ADDRESS.remove();
        return address;
    }

    public static com.destroystokyo.paper.profile.PlayerProfile takePreLoginProfile() {
        com.destroystokyo.paper.profile.PlayerProfile profile = PRE_LOGIN_PROFILE.get();
        PRE_LOGIN_PROFILE.remove();
        return profile;
    }

    public static void clearPreLoginContext() {
        PRE_LOGIN_HOSTNAME.remove();
        PRE_LOGIN_RAW_ADDRESS.remove();
        PRE_LOGIN_PROFILE.remove();
    }
}
