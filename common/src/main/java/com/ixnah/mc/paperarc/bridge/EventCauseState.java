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

    // ---- 触发点上下文（A8 批 3c）----

    private static final ThreadLocal<org.bukkit.inventory.EquipmentSlot> CAN_BUILD_HAND = new ThreadLocal<>();
    private static final ThreadLocal<org.bukkit.inventory.CookingRecipe<?>> COOK_RECIPE = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> FAST_REGEN = new ThreadLocal<>();
    private static final ThreadLocal<org.bukkit.util.Vector> VEHICLE_COLLISION_VELOCITY = new ThreadLocal<>();

    /**
     * {@code BlockCanBuildEvent#getHand()}。两个触发点（{@code BlockItem#canPlace} 与
     * {@code StandingAndWallBlockItem#getPlacementState}）在 HEAD 压同一个值，
     * 由事件构造器取走；不在触发点 RETURN 清，因为外层的
     * {@code StandingAndWallBlockItem} 要等它自己的 RETURN 才构造事件，
     * 内层 {@code canPlace} 一清就把值清没了。残留值会被下一次压入覆盖。
     */
    public static void setBlockCanBuildHand(org.bukkit.inventory.EquipmentSlot hand) {
        CAN_BUILD_HAND.set(hand);
    }

    public static org.bukkit.inventory.EquipmentSlot takeBlockCanBuildHand() {
        org.bukkit.inventory.EquipmentSlot hand = CAN_BUILD_HAND.get();
        CAN_BUILD_HAND.remove();
        return hand == null ? org.bukkit.inventory.EquipmentSlot.HAND : hand;
    }

    public static void setBlockCookRecipe(org.bukkit.inventory.CookingRecipe<?> recipe) {
        COOK_RECIPE.set(recipe);
    }

    public static org.bukkit.inventory.CookingRecipe<?> takeBlockCookRecipe() {
        org.bukkit.inventory.CookingRecipe<?> recipe = COOK_RECIPE.get();
        COOK_RECIPE.remove();
        return recipe;
    }

    public static void clearBlockCookRecipe() {
        COOK_RECIPE.remove();
    }

    public static void setFastRegen(boolean fastRegen) {
        FAST_REGEN.set(fastRegen);
    }

    public static boolean takeFastRegen() {
        Boolean fastRegen = FAST_REGEN.get();
        FAST_REGEN.remove();
        return fastRegen != null && fastRegen;
    }

    public static void clearFastRegen() {
        FAST_REGEN.remove();
    }

    public static void setVehicleCollisionVelocity(org.bukkit.util.Vector velocity) {
        VEHICLE_COLLISION_VELOCITY.set(velocity);
    }

    public static org.bukkit.util.Vector takeVehicleCollisionVelocity() {
        org.bukkit.util.Vector velocity = VEHICLE_COLLISION_VELOCITY.get();
        VEHICLE_COLLISION_VELOCITY.remove();
        return velocity;
    }

    public static void clearVehicleCollisionVelocity() {
        VEHICLE_COLLISION_VELOCITY.remove();
    }

    // ---- 触发点上下文（A8 批 3d）----

    private static final ThreadLocal<Boolean> DAMAGE_CRITICAL = new ThreadLocal<>();
    private static final ThreadLocal<net.kyori.adventure.text.Component> ADVANCEMENT_MESSAGE = new ThreadLocal<>();
    private static final ThreadLocal<org.bukkit.event.player.PlayerAdvancementDoneEvent> LAST_ADVANCEMENT_EVENT =
            new ThreadLocal<>();

    public static void setDamageCritical(boolean critical) {
        DAMAGE_CRITICAL.set(critical);
    }

    public static boolean takeDamageCritical() {
        Boolean critical = DAMAGE_CRITICAL.get();
        DAMAGE_CRITICAL.remove();
        return critical != null && critical;
    }

    public static void clearDamageCritical() {
        DAMAGE_CRITICAL.remove();
    }

    public static void setAdvancementMessage(net.kyori.adventure.text.Component message) {
        ADVANCEMENT_MESSAGE.set(message);
    }

    public static net.kyori.adventure.text.Component takeAdvancementMessage() {
        net.kyori.adventure.text.Component message = ADVANCEMENT_MESSAGE.get();
        ADVANCEMENT_MESSAGE.remove();
        return message;
    }

    public static void clearAdvancementMessage() {
        ADVANCEMENT_MESSAGE.remove();
    }

    /**
     * 最近一次构造出来的 {@code PlayerAdvancementDoneEvent}：广播那一步在
     * {@code PlayerAdvancements#award} 里、事件之后，要读回事件上的 {@code message()}。
     */
    public static void setLastAdvancementEvent(org.bukkit.event.player.PlayerAdvancementDoneEvent event) {
        LAST_ADVANCEMENT_EVENT.set(event);
    }

    public static org.bukkit.event.player.PlayerAdvancementDoneEvent takeLastAdvancementEvent() {
        org.bukkit.event.player.PlayerAdvancementDoneEvent event = LAST_ADVANCEMENT_EVENT.get();
        LAST_ADVANCEMENT_EVENT.remove();
        return event;
    }

    public static void clearLastAdvancementEvent() {
        LAST_ADVANCEMENT_EVENT.remove();
    }

    // ---- 事件字段 + 触发点消费（A8 批 3e）----

    private static final ThreadLocal<org.bukkit.event.inventory.FurnaceBurnEvent> LAST_FURNACE_BURN_EVENT =
            new ThreadLocal<>();
    private static final ThreadLocal<org.bukkit.event.player.PlayerPickupItemEvent> LAST_PICKUP_EVENT =
            new ThreadLocal<>();
    private static final ThreadLocal<org.bukkit.event.entity.EntityUnleashEvent> LAST_UNLEASH_EVENT =
            new ThreadLocal<>();
    private static final ThreadLocal<Boolean> UNLEASH_DROP_LEASH = new ThreadLocal<>();

    public static void setLastFurnaceBurnEvent(org.bukkit.event.inventory.FurnaceBurnEvent event) {
        LAST_FURNACE_BURN_EVENT.set(event);
    }

    public static org.bukkit.event.inventory.FurnaceBurnEvent takeLastFurnaceBurnEvent() {
        org.bukkit.event.inventory.FurnaceBurnEvent event = LAST_FURNACE_BURN_EVENT.get();
        LAST_FURNACE_BURN_EVENT.remove();
        return event;
    }

    public static void clearLastFurnaceBurnEvent() {
        LAST_FURNACE_BURN_EVENT.remove();
    }

    public static void setLastPickupEvent(org.bukkit.event.player.PlayerPickupItemEvent event) {
        LAST_PICKUP_EVENT.set(event);
    }

    public static org.bukkit.event.player.PlayerPickupItemEvent getLastPickupEvent() {
        return LAST_PICKUP_EVENT.get();
    }

    public static void clearLastPickupEvent() {
        LAST_PICKUP_EVENT.remove();
    }

    public static void setLastUnleashEvent(org.bukkit.event.entity.EntityUnleashEvent event) {
        LAST_UNLEASH_EVENT.set(event);
    }

    public static org.bukkit.event.entity.EntityUnleashEvent getLastUnleashEvent() {
        return LAST_UNLEASH_EVENT.get();
    }

    public static void clearLastUnleashEvent() {
        LAST_UNLEASH_EVENT.remove();
    }

    /**
     * {@code EntityUnleashEvent#isDropLeash()} 的默认值。
     * paper 在六个调用点各传一个字面量：四处是 {@code true}（tickLeash / startRiding），
     * {@code removeAfterChangingDimensions} 是 {@code false}，
     * 玩家解绳那两处是 {@code !instabuild}。默认按多数取 {@code true}，
     * 另外三处在触发点 HEAD 压值。
     */
    public static void setUnleashDropLeash(boolean dropLeash) {
        UNLEASH_DROP_LEASH.set(dropLeash);
    }

    public static boolean takeUnleashDropLeash() {
        Boolean dropLeash = UNLEASH_DROP_LEASH.get();
        UNLEASH_DROP_LEASH.remove();
        return dropLeash == null || dropLeash;
    }

    public static void clearUnleashDropLeash() {
        UNLEASH_DROP_LEASH.remove();
    }

    // ---- 事件字段 + 触发点消费（A8 批 3f）----

    private static final ThreadLocal<org.bukkit.event.player.PlayerItemConsumeEvent> LAST_CONSUME_EVENT =
            new ThreadLocal<>();
    private static final ThreadLocal<org.bukkit.event.player.PlayerItemMendEvent> LAST_MEND_EVENT =
            new ThreadLocal<>();
    private static final ThreadLocal<org.bukkit.event.inventory.InventoryOpenEvent> LAST_INVENTORY_OPEN_EVENT =
            new ThreadLocal<>();

    public static void setLastConsumeEvent(org.bukkit.event.player.PlayerItemConsumeEvent event) {
        LAST_CONSUME_EVENT.set(event);
    }

    public static org.bukkit.event.player.PlayerItemConsumeEvent takeLastConsumeEvent() {
        org.bukkit.event.player.PlayerItemConsumeEvent event = LAST_CONSUME_EVENT.get();
        LAST_CONSUME_EVENT.remove();
        return event;
    }

    public static void clearLastConsumeEvent() {
        LAST_CONSUME_EVENT.remove();
    }

    public static void setLastMendEvent(org.bukkit.event.player.PlayerItemMendEvent event) {
        LAST_MEND_EVENT.set(event);
    }

    public static org.bukkit.event.player.PlayerItemMendEvent takeLastMendEvent() {
        org.bukkit.event.player.PlayerItemMendEvent event = LAST_MEND_EVENT.get();
        LAST_MEND_EVENT.remove();
        return event;
    }

    public static void clearLastMendEvent() {
        LAST_MEND_EVENT.remove();
    }

    public static void setLastInventoryOpenEvent(org.bukkit.event.inventory.InventoryOpenEvent event) {
        LAST_INVENTORY_OPEN_EVENT.set(event);
    }

    public static org.bukkit.event.inventory.InventoryOpenEvent takeLastInventoryOpenEvent() {
        org.bukkit.event.inventory.InventoryOpenEvent event = LAST_INVENTORY_OPEN_EVENT.get();
        LAST_INVENTORY_OPEN_EVENT.remove();
        return event;
    }

    public static void clearLastInventoryOpenEvent() {
        LAST_INVENTORY_OPEN_EVENT.remove();
    }

    // ---- PlayerPurchaseEvent 的两个开关（A8/Y-4，gaps.md E2）----

    private static final ThreadLocal<io.papermc.paper.event.player.PlayerPurchaseEvent> LAST_PURCHASE_EVENT =
            new ThreadLocal<>();

    public static void setLastPurchaseEvent(io.papermc.paper.event.player.PlayerPurchaseEvent event) {
        LAST_PURCHASE_EVENT.set(event);
    }

    public static io.papermc.paper.event.player.PlayerPurchaseEvent getLastPurchaseEvent() {
        return LAST_PURCHASE_EVENT.get();
    }

    public static void clearLastPurchaseEvent() {
        LAST_PURCHASE_EVENT.remove();
    }
}
