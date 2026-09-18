package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.Bukkit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 加在 {@code org.bukkit.Bukkit} 静态门面上的方法（checklist §1.10 am，第 ① 批）。
 *
 * <p>{@code Bukkit} 是类不是接口，{@code audit.py} 的三层审计完全照不到；paper 版
 * {@code Bukkit} 的每个方法体都只是 {@code server.xxx(...)} 一行转发，Server 侧的对应
 * 方法在 B/B2/B3 已经补齐，所以这里逐条照搬转发即可。</p>
 *
 * <p>用 {@code @Unique public static}：Mixin 会把静态方法一并合并进目标类。</p>
 */
@Mixin(Bukkit.class)
public abstract class BukkitApiMixin {

    @Unique
    public static java.io.File getPluginsFolder() {
        return Bukkit.getServer().getPluginsFolder();
    }

    /**
     * paper 的实现用 {@code io.papermc.paper.ServerBuildInfo}（运行时没有），
     * 这里按同样的格式用运行时已有的三个字符串拼。
     */
    @Unique
    public static java.lang.String getVersionMessage() {
        return Bukkit.getName() + " version " + Bukkit.getVersion()
                + " (Implementing API version " + Bukkit.getBukkitVersion() + ")";
    }

    @Unique
    public static java.lang.String getMinecraftVersion() {
        return Bukkit.getServer().getMinecraftVersion();
    }

    @Unique
    public static void broadcast(net.md_5.bungee.api.chat.BaseComponent p0) {
        Bukkit.getServer().broadcast(p0);
    }

    @Unique
    public static void broadcast(net.md_5.bungee.api.chat.BaseComponent[] p0) {
        Bukkit.getServer().broadcast(p0);
    }

    @Unique
    public static java.util.UUID getPlayerUniqueId(java.lang.String p0) {
        return Bukkit.getServer().getPlayerUniqueId(p0);
    }

    @Unique
    public static boolean isTickingWorlds() {
        return Bukkit.getServer().isTickingWorlds();
    }

    @Unique
    public static org.bukkit.World getWorld(org.bukkit.NamespacedKey p0) {
        return Bukkit.getServer().getWorld(p0);
    }

    @Unique
    public static org.bukkit.World getWorld(net.kyori.adventure.key.Key p0) {
        return Bukkit.getServer().getWorld(p0);
    }

    @Unique
    public static org.bukkit.inventory.ItemStack createExplorerMap(org.bukkit.World p0, org.bukkit.Location p1, org.bukkit.generator.structure.StructureType p2, org.bukkit.map.MapCursor.Type p3) {
        return Bukkit.getServer().createExplorerMap(p0, p1, p2, p3);
    }

    @Unique
    public static org.bukkit.inventory.ItemStack createExplorerMap(org.bukkit.World p0, org.bukkit.Location p1, org.bukkit.generator.structure.StructureType p2, org.bukkit.map.MapCursor.Type p3, int p4, boolean p5) {
        return Bukkit.getServer().createExplorerMap(p0, p1, p2, p3, p4, p5);
    }

    @Unique
    public static void updateResources() {
        Bukkit.getServer().updateResources();
    }

    @Unique
    public static void updateRecipes() {
        Bukkit.getServer().updateRecipes();
    }

    @Unique
    public static boolean addRecipe(org.bukkit.inventory.Recipe p0, boolean p1) {
        return Bukkit.getServer().addRecipe(p0, p1);
    }

    @Unique
    public static boolean removeRecipe(org.bukkit.NamespacedKey p0, boolean p1) {
        return Bukkit.getServer().removeRecipe(p0, p1);
    }

    @Unique
    public static int broadcast(net.kyori.adventure.text.Component p0) {
        return Bukkit.getServer().broadcast(p0);
    }

    @Unique
    public static int broadcast(net.kyori.adventure.text.Component p0, java.lang.String p1) {
        return Bukkit.getServer().broadcast(p0, p1);
    }

    @Unique
    public static org.bukkit.OfflinePlayer getOfflinePlayerIfCached(java.lang.String p0) {
        return Bukkit.getServer().getOfflinePlayerIfCached(p0);
    }

    @Unique
    public static org.bukkit.BanList getBanList(io.papermc.paper.ban.BanListType p0) {
        return Bukkit.getServer().getBanList(p0);
    }

    @Unique
    public static org.bukkit.command.CommandSender createCommandSender(java.util.function.Consumer p0) {
        return Bukkit.getServer().createCommandSender(p0);
    }

    @Unique
    public static org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder p0, org.bukkit.event.inventory.InventoryType p1, net.kyori.adventure.text.Component p2) {
        return Bukkit.getServer().createInventory(p0, p1, p2);
    }

    @Unique
    public static org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder p0, int p1, net.kyori.adventure.text.Component p2) {
        return Bukkit.getServer().createInventory(p0, p1, p2);
    }

    @Unique
    public static org.bukkit.inventory.Merchant createMerchant(net.kyori.adventure.text.Component p0) {
        return Bukkit.getServer().createMerchant(p0);
    }

    @Unique
    public static net.kyori.adventure.text.Component motd() {
        return Bukkit.getServer().motd();
    }

    @Unique
    public static void motd(net.kyori.adventure.text.Component p0) {
        Bukkit.getServer().motd(p0);
    }

    @Unique
    public static net.kyori.adventure.text.Component shutdownMessage() {
        return Bukkit.getServer().shutdownMessage();
    }

    @Unique
    public static double[] getTPS() {
        return Bukkit.getServer().getTPS();
    }

    @Unique
    public static long[] getTickTimes() {
        return Bukkit.getServer().getTickTimes();
    }

    @Unique
    public static double getAverageTickTime() {
        return Bukkit.getServer().getAverageTickTime();
    }

    @Unique
    public static org.bukkit.command.CommandMap getCommandMap() {
        return Bukkit.getServer().getCommandMap();
    }

    @Unique
    public static void reloadPermissions() {
        Bukkit.getServer().reloadPermissions();
    }

    @Unique
    public static boolean reloadCommandAliases() {
        return Bukkit.getServer().reloadCommandAliases();
    }

    @Unique
    public static boolean suggestPlayerNamesWhenNullTabCompletions() {
        return Bukkit.getServer().suggestPlayerNamesWhenNullTabCompletions();
    }

    @Unique
    public static java.lang.String getPermissionMessage() {
        return Bukkit.getServer().getPermissionMessage();
    }

    @Unique
    public static net.kyori.adventure.text.Component permissionMessage() {
        return Bukkit.getServer().permissionMessage();
    }

    @Unique
    public static com.destroystokyo.paper.profile.PlayerProfile createProfile(java.util.UUID p0) {
        return Bukkit.getServer().createProfile(p0);
    }

    @Unique
    public static com.destroystokyo.paper.profile.PlayerProfile createProfile(java.lang.String p0) {
        return Bukkit.getServer().createProfile(p0);
    }

    @Unique
    public static com.destroystokyo.paper.profile.PlayerProfile createProfile(java.util.UUID p0, java.lang.String p1) {
        return Bukkit.getServer().createProfile(p0, p1);
    }

    @Unique
    public static com.destroystokyo.paper.profile.PlayerProfile createProfileExact(java.util.UUID p0, java.lang.String p1) {
        return Bukkit.getServer().createProfileExact(p0, p1);
    }

    @Unique
    public static int getCurrentTick() {
        return Bukkit.getServer().getCurrentTick();
    }

    @Unique
    public static boolean isStopping() {
        return Bukkit.getServer().isStopping();
    }

    @Unique
    public static com.destroystokyo.paper.entity.ai.MobGoals getMobGoals() {
        return Bukkit.getServer().getMobGoals();
    }

    @Unique
    public static io.papermc.paper.datapack.DatapackManager getDatapackManager() {
        return Bukkit.getServer().getDatapackManager();
    }

    @Unique
    public static org.bukkit.potion.PotionBrewer getPotionBrewer() {
        return Bukkit.getServer().getPotionBrewer();
    }

    @Unique
    public static io.papermc.paper.threadedregions.scheduler.RegionScheduler getRegionScheduler() {
        return Bukkit.getServer().getRegionScheduler();
    }

    @Unique
    public static io.papermc.paper.threadedregions.scheduler.AsyncScheduler getAsyncScheduler() {
        return Bukkit.getServer().getAsyncScheduler();
    }

    @Unique
    public static io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler getGlobalRegionScheduler() {
        return Bukkit.getServer().getGlobalRegionScheduler();
    }

    @Unique
    public static boolean isOwnedByCurrentRegion(org.bukkit.World p0, io.papermc.paper.math.Position p1) {
        return Bukkit.getServer().isOwnedByCurrentRegion(p0, p1);
    }

    @Unique
    public static boolean isOwnedByCurrentRegion(org.bukkit.World p0, io.papermc.paper.math.Position p1, int p2) {
        return Bukkit.getServer().isOwnedByCurrentRegion(p0, p1, p2);
    }

    @Unique
    public static boolean isOwnedByCurrentRegion(org.bukkit.Location p0) {
        return Bukkit.getServer().isOwnedByCurrentRegion(p0);
    }

    @Unique
    public static boolean isOwnedByCurrentRegion(org.bukkit.Location p0, int p1) {
        return Bukkit.getServer().isOwnedByCurrentRegion(p0, p1);
    }

    @Unique
    public static boolean isOwnedByCurrentRegion(org.bukkit.block.Block p0) {
        return Bukkit.getServer().isOwnedByCurrentRegion(p0);
    }

    @Unique
    public static boolean isOwnedByCurrentRegion(org.bukkit.World p0, int p1, int p2) {
        return Bukkit.getServer().isOwnedByCurrentRegion(p0, p1, p2);
    }

    @Unique
    public static boolean isOwnedByCurrentRegion(org.bukkit.World p0, int p1, int p2, int p3) {
        return Bukkit.getServer().isOwnedByCurrentRegion(p0, p1, p2, p3);
    }

    @Unique
    public static boolean isOwnedByCurrentRegion(org.bukkit.entity.Entity p0) {
        return Bukkit.getServer().isOwnedByCurrentRegion(p0);
    }
}
