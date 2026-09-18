package com.ixnah.mc.paperarc.bridge.api;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
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
    private static final ThreadLocal<EntityPortalEnterEvent> LAST_PORTAL_ENTER = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> TELEPORT_DISMOUNT = new ThreadLocal<>();

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

    public static void rememberPortalEnterEvent(EntityPortalEnterEvent event) {
        LAST_PORTAL_ENTER.set(event);
    }

    public static EntityPortalEnterEvent takePortalEnterEvent() {
        EntityPortalEnterEvent event = LAST_PORTAL_ENTER.get();
        LAST_PORTAL_ENTER.remove();
        return event;
    }

    public static void pushTeleportDismount(boolean dismount) {
        TELEPORT_DISMOUNT.set(dismount);
    }

    public static void popTeleportDismount() {
        TELEPORT_DISMOUNT.remove();
    }

    /** 默认 true：不带 {@code TeleportFlag} 的传送在 CraftBukkit 里本来就会把玩家从坐骑上弄下来。 */
    public static boolean teleportDismount() {
        Boolean dismount = TELEPORT_DISMOUNT.get();
        return dismount == null || dismount;
    }

    // ---- Y-2 批 3b：触发点上下文（事件构造器读回）----

    private static final ThreadLocal<org.bukkit.block.BlockFace> BLOCK_DAMAGE_FACE = new ThreadLocal<>();
    private static final ThreadLocal<org.bukkit.entity.Entity> EXP_SOURCE = new ThreadLocal<>();
    private static final ThreadLocal<org.bukkit.inventory.EquipmentSlot> CAN_BUILD_HAND = new ThreadLocal<>();
    private static final ThreadLocal<org.bukkit.inventory.CookingRecipe<?>> COOK_RECIPE = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> FAST_REGEN = new ThreadLocal<>();
    private static final ThreadLocal<org.bukkit.util.Vector> VEHICLE_COLLISION_VELOCITY = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> DAMAGE_CRITICAL = new ThreadLocal<>();
    private static final ThreadLocal<net.kyori.adventure.text.Component> ADVANCEMENT_MESSAGE = new ThreadLocal<>();
    private static final ThreadLocal<org.bukkit.event.player.PlayerAdvancementDoneEvent> LAST_ADVANCEMENT_EVENT =
            new ThreadLocal<>();
    private static final ThreadLocal<Boolean> ADVANCEMENT_BROADCASTING = new ThreadLocal<>();

    public static void pushBlockDamageFace(org.bukkit.block.BlockFace face) {
        BLOCK_DAMAGE_FACE.set(face);
    }

    public static org.bukkit.block.BlockFace takeBlockDamageFace() {
        org.bukkit.block.BlockFace face = BLOCK_DAMAGE_FACE.get();
        BLOCK_DAMAGE_FACE.remove();
        return face;
    }

    public static void popBlockDamageFace() {
        BLOCK_DAMAGE_FACE.remove();
    }

    public static void pushExperienceSource(org.bukkit.entity.Entity source) {
        EXP_SOURCE.set(source);
    }

    public static org.bukkit.entity.Entity takeExperienceSource() {
        org.bukkit.entity.Entity source = EXP_SOURCE.get();
        EXP_SOURCE.remove();
        return source;
    }

    public static void popExperienceSource() {
        EXP_SOURCE.remove();
    }

    /**
     * {@code BlockCanBuildEvent#getHand()}。两个触发点（{@code BlockItem#canPlace} 与
     * {@code StandingAndWallBlockItem#getPlacementState}）在 HEAD 压同一个值；
     * 不在触发点 RETURN 清 —— 外层的 {@code StandingAndWallBlockItem} 要等自己 RETURN
     * 才构造事件，内层 {@code canPlace} 一清就把值清没了。残留值会被下一次压入覆盖。
     */
    public static void pushBlockCanBuildHand(org.bukkit.inventory.EquipmentSlot hand) {
        CAN_BUILD_HAND.set(hand);
    }

    public static org.bukkit.inventory.EquipmentSlot takeBlockCanBuildHand() {
        org.bukkit.inventory.EquipmentSlot hand = CAN_BUILD_HAND.get();
        CAN_BUILD_HAND.remove();
        return hand == null ? org.bukkit.inventory.EquipmentSlot.HAND : hand;
    }

    public static void pushBlockCookRecipe(org.bukkit.inventory.CookingRecipe<?> recipe) {
        COOK_RECIPE.set(recipe);
    }

    public static org.bukkit.inventory.CookingRecipe<?> takeBlockCookRecipe() {
        org.bukkit.inventory.CookingRecipe<?> recipe = COOK_RECIPE.get();
        COOK_RECIPE.remove();
        return recipe;
    }

    public static void popBlockCookRecipe() {
        COOK_RECIPE.remove();
    }

    public static void pushFastRegen(boolean fastRegen) {
        FAST_REGEN.set(fastRegen);
    }

    public static boolean takeFastRegen() {
        Boolean fastRegen = FAST_REGEN.get();
        FAST_REGEN.remove();
        return fastRegen != null && fastRegen;
    }

    public static void popFastRegen() {
        FAST_REGEN.remove();
    }

    public static void pushVehicleCollisionVelocity(org.bukkit.util.Vector velocity) {
        VEHICLE_COLLISION_VELOCITY.set(velocity);
    }

    public static org.bukkit.util.Vector takeVehicleCollisionVelocity() {
        org.bukkit.util.Vector velocity = VEHICLE_COLLISION_VELOCITY.get();
        VEHICLE_COLLISION_VELOCITY.remove();
        return velocity;
    }

    public static void popVehicleCollisionVelocity() {
        VEHICLE_COLLISION_VELOCITY.remove();
    }

    public static void pushDamageCritical(boolean critical) {
        DAMAGE_CRITICAL.set(critical);
    }

    public static boolean takeDamageCritical() {
        Boolean critical = DAMAGE_CRITICAL.get();
        DAMAGE_CRITICAL.remove();
        return critical != null && critical;
    }

    public static void popDamageCritical() {
        DAMAGE_CRITICAL.remove();
    }

    public static void pushAdvancementMessage(net.kyori.adventure.text.Component message) {
        ADVANCEMENT_MESSAGE.set(message);
    }

    public static net.kyori.adventure.text.Component takeAdvancementMessage() {
        net.kyori.adventure.text.Component message = ADVANCEMENT_MESSAGE.get();
        ADVANCEMENT_MESSAGE.remove();
        return message;
    }

    public static void popAdvancementMessage() {
        ADVANCEMENT_MESSAGE.remove();
    }

    public static void rememberAdvancementEvent(org.bukkit.event.player.PlayerAdvancementDoneEvent event) {
        LAST_ADVANCEMENT_EVENT.set(event);
    }

    public static org.bukkit.event.player.PlayerAdvancementDoneEvent advancementEvent() {
        return LAST_ADVANCEMENT_EVENT.get();
    }

    public static void popAdvancementEvent() {
        LAST_ADVANCEMENT_EVENT.remove();
    }

    /**
     * 进度广播窗口：只在 {@code PlayerAdvancements#award} 走到那句
     * {@code Optional.ifPresent(this::广播)} 之后才打开，关在 {@code award} 的 RETURN。
     * 插件在 {@code PlayerAdvancementDoneEvent}（更早，锚在 {@code AdvancementRewards#grant}）
     * 里自己发的广播落在窗口之外，不会被我们改写。
     */
    public static void openAdvancementBroadcast() {
        ADVANCEMENT_BROADCASTING.set(Boolean.TRUE);
    }

    public static boolean advancementBroadcasting() {
        return Boolean.TRUE.equals(ADVANCEMENT_BROADCASTING.get());
    }

    public static void closeAdvancementBroadcast() {
        ADVANCEMENT_BROADCASTING.remove();
    }

    // ---- Y-2 批 3c：事件字段 + 触发点消费（"最近一次事件"通道）----
    //
    // 这一批的方向与上面相反：不是把上下文压给事件，而是把**事件对象**留给触发点，
    // 让触发点读回插件可能改过的值。事件都由 Arclight 在它自己的注入器 handler 里构造
    // （方法名带加载器相关随机段，选不了），只能靠事件构造器自己登记。

    private static final ThreadLocal<org.bukkit.event.inventory.FurnaceBurnEvent> LAST_FURNACE_BURN =
            new ThreadLocal<>();
    private static final ThreadLocal<org.bukkit.event.player.PlayerItemConsumeEvent> LAST_ITEM_CONSUME =
            new ThreadLocal<>();
    private static final ThreadLocal<org.bukkit.event.player.PlayerPickupItemEvent> LAST_PICKUP =
            new ThreadLocal<>();
    private static final ThreadLocal<org.bukkit.event.inventory.InventoryOpenEvent> LAST_INVENTORY_OPEN =
            new ThreadLocal<>();
    private static final ThreadLocal<org.bukkit.event.entity.EntityUnleashEvent> LAST_UNLEASH =
            new ThreadLocal<>();
    private static final ThreadLocal<Boolean> UNLEASH_DROP_LEASH = new ThreadLocal<>();
    private static final ThreadLocal<Integer> PICKUP_ORIGINAL_COUNT = new ThreadLocal<>();

    public static void rememberFurnaceBurnEvent(org.bukkit.event.inventory.FurnaceBurnEvent event) {
        LAST_FURNACE_BURN.set(event);
    }

    public static org.bukkit.event.inventory.FurnaceBurnEvent takeFurnaceBurnEvent() {
        org.bukkit.event.inventory.FurnaceBurnEvent event = LAST_FURNACE_BURN.get();
        LAST_FURNACE_BURN.remove();
        return event;
    }

    public static void popFurnaceBurnEvent() {
        LAST_FURNACE_BURN.remove();
    }

    public static void rememberItemConsumeEvent(org.bukkit.event.player.PlayerItemConsumeEvent event) {
        LAST_ITEM_CONSUME.set(event);
    }

    public static org.bukkit.event.player.PlayerItemConsumeEvent takeItemConsumeEvent() {
        org.bukkit.event.player.PlayerItemConsumeEvent event = LAST_ITEM_CONSUME.get();
        LAST_ITEM_CONSUME.remove();
        return event;
    }

    public static void popItemConsumeEvent() {
        LAST_ITEM_CONSUME.remove();
    }

    public static void rememberPickupEvent(org.bukkit.event.player.PlayerPickupItemEvent event) {
        LAST_PICKUP.set(event);
    }

    public static org.bukkit.event.player.PlayerPickupItemEvent pickupEvent() {
        return LAST_PICKUP.get();
    }

    public static void popPickupEvent() {
        LAST_PICKUP.remove();
        PICKUP_ORIGINAL_COUNT.remove();
    }

    /** 拾取前的原始堆叠数：paper 传给 {@code Player#take} 的那个数，在 HEAD 取。 */
    public static void pushPickupOriginalCount(int count) {
        PICKUP_ORIGINAL_COUNT.set(count);
    }

    public static int pickupOriginalCount() {
        Integer count = PICKUP_ORIGINAL_COUNT.get();
        return count == null ? 1 : count;
    }

    public static void rememberInventoryOpenEvent(org.bukkit.event.inventory.InventoryOpenEvent event) {
        LAST_INVENTORY_OPEN.set(event);
    }

    public static org.bukkit.event.inventory.InventoryOpenEvent takeInventoryOpenEvent() {
        org.bukkit.event.inventory.InventoryOpenEvent event = LAST_INVENTORY_OPEN.get();
        LAST_INVENTORY_OPEN.remove();
        return event;
    }

    public static void rememberUnleashEvent(org.bukkit.event.entity.EntityUnleashEvent event) {
        LAST_UNLEASH.set(event);
    }

    public static org.bukkit.event.entity.EntityUnleashEvent unleashEvent() {
        return LAST_UNLEASH.get();
    }

    public static void popUnleashEvent() {
        LAST_UNLEASH.remove();
    }

    /**
     * {@code EntityUnleashEvent#isDropLeash()} 的默认值。paper 在各调用点传字面量：
     * 多数是 {@code true}，换维度是 {@code false}，玩家解绳是 {@code !instabuild}。
     * 默认按多数取 {@code true}。
     */
    public static void pushUnleashDropLeash(boolean dropLeash) {
        UNLEASH_DROP_LEASH.set(dropLeash);
    }

    public static boolean takeUnleashDropLeash() {
        Boolean dropLeash = UNLEASH_DROP_LEASH.get();
        UNLEASH_DROP_LEASH.remove();
        return dropLeash == null || dropLeash;
    }

    // ---- Y-2 批 3d：AsyncPlayerPreLoginEvent 的三样附加上下文 ----

    private static final ThreadLocal<String> PRE_LOGIN_HOSTNAME = new ThreadLocal<>();
    private static final ThreadLocal<java.net.InetAddress> PRE_LOGIN_RAW_ADDRESS = new ThreadLocal<>();
    private static final ThreadLocal<com.destroystokyo.paper.profile.PlayerProfile> PRE_LOGIN_PROFILE =
            new ThreadLocal<>();

    public static void pushPreLoginContext(String hostname, java.net.InetAddress rawAddress,
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

    public static void popPreLoginContext() {
        PRE_LOGIN_HOSTNAME.remove();
        PRE_LOGIN_RAW_ADDRESS.remove();
        PRE_LOGIN_PROFILE.remove();
    }

    // ---- Y-4：PlayerPurchaseEvent 的两个开关（gaps.md E2）----

    private static final ThreadLocal<io.papermc.paper.event.player.PlayerPurchaseEvent> LAST_PURCHASE =
            new ThreadLocal<>();

    public static void rememberPurchaseEvent(io.papermc.paper.event.player.PlayerPurchaseEvent event) {
        LAST_PURCHASE.set(event);
    }

    public static io.papermc.paper.event.player.PlayerPurchaseEvent purchaseEvent() {
        return LAST_PURCHASE.get();
    }

    public static void popPurchaseEvent() {
        LAST_PURCHASE.remove();
    }
}
