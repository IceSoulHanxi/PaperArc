package com.ixnah.mc.paperarc.bridge.api;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

/**
 * gaps.md §3.1「Cause/Reason」一组共用的 ThreadLocal 上下文（`LaunchState` 式）。
 *
 * <p>为什么是 ThreadLocal 而不是在事件构造点 `@WrapOperation`：这些事件都由 Arclight 自己的
 * mixin 构造，而那些构造点落在 Arclight 的注入器 handler 里，方法名被 Mixin 改成
 * {@code handler$<id>$…}/{@code decorate$<id>$…}，{@code <id>} 三个加载器各不相同、选不了
 * （见 docs/mixin-conventions.md）。能稳定锚的是**上游的 vanilla 调用点**：在那里压一个原因，
 * 事件类自己的构造器（也是我们的 mixin）读回来即可 —— 无论中间隔着 Arclight 的哪一层。
 *
 * <p>每个 push 都必须有配对的 pop（用 try/finally 或 {@code @WrapOperation} 包住原调用）。
 */
public final class PaperarcEventCauses {

    private static final ThreadLocal<WeatherChangeEvent.Cause> WEATHER = new ThreadLocal<>();
    private static final ThreadLocal<ThunderChangeEvent.Cause> THUNDER = new ThreadLocal<>();
    private static final ThreadLocal<InventoryCloseEvent.Reason> INVENTORY_CLOSE = new ThreadLocal<>();
    private static final ThreadLocal<PlayerGameModeChangeEvent.Cause> GAME_MODE = new ThreadLocal<>();
    private static final ThreadLocal<PlayerGameModeChangeEvent> LAST_GAME_MODE_EVENT = new ThreadLocal<>();
    private static final ThreadLocal<PlayerKickEvent.Cause> KICK = new ThreadLocal<>();

    private PaperarcEventCauses() {
    }

    public static void pushWeather(WeatherChangeEvent.Cause cause) {
        WEATHER.set(cause);
    }

    public static void popWeather() {
        WEATHER.remove();
    }

    public static WeatherChangeEvent.Cause weather() {
        WeatherChangeEvent.Cause cause = WEATHER.get();
        return cause == null ? WeatherChangeEvent.Cause.UNKNOWN : cause;
    }

    public static void pushThunder(ThunderChangeEvent.Cause cause) {
        THUNDER.set(cause);
    }

    public static void popThunder() {
        THUNDER.remove();
    }

    public static ThunderChangeEvent.Cause thunder() {
        ThunderChangeEvent.Cause cause = THUNDER.get();
        return cause == null ? ThunderChangeEvent.Cause.UNKNOWN : cause;
    }

    public static void pushInventoryClose(InventoryCloseEvent.Reason reason) {
        INVENTORY_CLOSE.set(reason);
    }

    public static void popInventoryClose() {
        INVENTORY_CLOSE.remove();
    }

    public static InventoryCloseEvent.Reason inventoryClose() {
        InventoryCloseEvent.Reason reason = INVENTORY_CLOSE.get();
        return reason == null ? InventoryCloseEvent.Reason.UNKNOWN : reason;
    }

    public static void pushGameMode(PlayerGameModeChangeEvent.Cause cause) {
        GAME_MODE.set(cause);
    }

    public static void popGameMode() {
        GAME_MODE.remove();
    }

    public static PlayerGameModeChangeEvent.Cause gameMode() {
        PlayerGameModeChangeEvent.Cause cause = GAME_MODE.get();
        return cause == null ? PlayerGameModeChangeEvent.Cause.UNKNOWN : cause;
    }

    public static void pushKick(PlayerKickEvent.Cause cause) {
        KICK.set(cause);
    }

    public static void popKick() {
        KICK.remove();
    }

    public static PlayerKickEvent.Cause kick() {
        PlayerKickEvent.Cause cause = KICK.get();
        return cause == null ? PlayerKickEvent.Cause.UNKNOWN : cause;
    }

    /**
     * 断开原因 → {@code PlayerKickEvent.Cause}。Paper 是在三十来个 {@code disconnect(...)}
     * 调用点各传一个 Cause；那些调用点大多在 Arclight 改不动/锚不住的位置，这里改成在唯一的
     * 汇聚点 {@code ServerCommonPacketListenerImpl#disconnect} 按**原版的翻译键**反查 ——
     * 键是 vanilla 自己写死的字面量，不随语言变。查不到就 UNKNOWN（与 Paper 对没有显式
     * Cause 的调用点一致）。插件那条（PLUGIN）与 {@code /kick}（KICK_COMMAND）另有各自的锚点。
     */
    public static PlayerKickEvent.Cause causeFromDisconnectReason(Component reason) {
        if (reason == null || !(reason.getContents() instanceof TranslatableContents translatable)) {
            return PlayerKickEvent.Cause.UNKNOWN;
        }
        return switch (translatable.getKey()) {
            case "disconnect.timeout" -> PlayerKickEvent.Cause.TIMEOUT;
            case "disconnect.spam" -> PlayerKickEvent.Cause.SPAM;
            case "multiplayer.disconnect.idling" -> PlayerKickEvent.Cause.IDLING;
            case "multiplayer.disconnect.banned", "multiplayer.disconnect.banned.reason" -> PlayerKickEvent.Cause.BANNED;
            case "multiplayer.disconnect.ip_banned", "multiplayer.disconnect.banned_ip.reason" -> PlayerKickEvent.Cause.IP_BANNED;
            case "multiplayer.disconnect.not_whitelisted" -> PlayerKickEvent.Cause.WHITELIST;
            case "multiplayer.disconnect.duplicate_login" -> PlayerKickEvent.Cause.DUPLICATE_LOGIN;
            case "multiplayer.disconnect.flying" -> PlayerKickEvent.Cause.FLYING_PLAYER;
            case "multiplayer.disconnect.invalid_player_movement" -> PlayerKickEvent.Cause.INVALID_PLAYER_MOVEMENT;
            case "multiplayer.disconnect.invalid_vehicle_movement" -> PlayerKickEvent.Cause.INVALID_VEHICLE_MOVEMENT;
            case "multiplayer.disconnect.invalid_entity_attacked" -> PlayerKickEvent.Cause.INVALID_ENTITY_ATTACKED;
            case "multiplayer.disconnect.illegal_characters" -> PlayerKickEvent.Cause.ILLEGAL_CHARACTERS;
            case "multiplayer.disconnect.out_of_order_chat" -> PlayerKickEvent.Cause.OUT_OF_ORDER_CHAT;
            case "multiplayer.disconnect.too_many_pending_chats" -> PlayerKickEvent.Cause.TOO_MANY_PENDING_CHATS;
            case "multiplayer.disconnect.chat_validation_failed" -> PlayerKickEvent.Cause.CHAT_VALIDATION_FAILED;
            case "multiplayer.disconnect.unsigned_chat" -> PlayerKickEvent.Cause.UNSIGNED_CHAT;
            case "multiplayer.disconnect.expired_public_key" -> PlayerKickEvent.Cause.EXPIRED_PROFILE_PUBLIC_KEY;
            case "multiplayer.disconnect.invalid_public_key_signature" -> PlayerKickEvent.Cause.INVALID_PUBLIC_KEY_SIGNATURE;
            case "multiplayer.requiredTexturePrompt.disconnect" -> PlayerKickEvent.Cause.RESOURCE_PACK_REJECTION;
            case "multiplayer.disconnect.server_shutdown" -> PlayerKickEvent.Cause.RESTART_COMMAND;
            default -> PlayerKickEvent.Cause.UNKNOWN;
        };
    }

    public static void rememberGameModeEvent(PlayerGameModeChangeEvent event) {
        LAST_GAME_MODE_EVENT.set(event);
    }

    public static PlayerGameModeChangeEvent takeGameModeEvent() {
        PlayerGameModeChangeEvent event = LAST_GAME_MODE_EVENT.get();
        LAST_GAME_MODE_EVENT.remove();
        return event;
    }
}
