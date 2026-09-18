package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.Bukkit;
import com.ixnah.mc.paperarc.mixin.annotation.Widen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 加在 {@code org.bukkit.Bukkit} 静态门面上的方法（checklist §1.10 am 第一批，共 53 条）。
 *
 * <p>{@code Bukkit} 在 paper 里全是 {@code server.xxx(...)} 一行转发，所以这里也一律转发到
 * {@code Bukkit.getServer()} —— {@code Server} 侧的 paper 方法在 A2/A4/A5 已经补齐
 * （{@code audit.py} 接口维度为 0），转发不会调到运行时不存在的成员。
 * 例外两处：{@code broadcast(BaseComponent...)} 走 {@code Server.Spigot}（运行时已有），
 * {@code getVersionMessage()} 在 paper 里也没有对应的 Server 方法，按同样口径自己拼。
 *
 * <p>为什么必须补：{@code Bukkit} 是**类**不是接口，{@code audit.py} 只比接口，
 * 这 53 条缺了插件调到就是 {@code NoSuchMethodError}（清单
 * {@code docs/data/1201-class-method-gaps.md}）。
 */
@Mixin(Bukkit.class)
public abstract class BukkitApiMixin {

    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static java.io.File getPluginsFolder() {
        return Bukkit.getServer().getPluginsFolder();
    }
    /**
     * 与 Paper 同一句话（Add-Git-information-to-version-command-on-startup）。
     * Arclight 的服务端 jar 清单里没有 Git-Branch/Git-Commit，取不到时省掉
     * {@code (Git: …)} 后缀，而不是打印 "(Git: null)"。
     */
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static String getVersionMessage() {
        String gitBranch = null;
        String gitCommit = null;
        try {
            java.net.URL source = Bukkit.getServer().getClass().getProtectionDomain()
                    .getCodeSource().getLocation();
            try (java.io.InputStream in = new java.net.URL("jar:" + source + "!/META-INF/MANIFEST.MF")
                    .openStream()) {
                java.util.jar.Manifest manifest = new java.util.jar.Manifest(in);
                gitBranch = manifest.getMainAttributes().getValue("Git-Branch");
                gitCommit = manifest.getMainAttributes().getValue("Git-Commit");
            }
        } catch (Exception ignored) {
            // 没有清单或读不到就按无 Git 信息处理
        }
        String base = "This server is running " + Bukkit.getName() + " version " + Bukkit.getVersion()
                + " (Implementing API version " + Bukkit.getBukkitVersion() + ")";
        if (gitCommit == null) {
            return base;
        }
        String branchMsg = ("master".equals(gitBranch) || "main".equals(gitBranch) || gitBranch == null)
                ? "" : " on " + gitBranch;
        return base + " (Git: " + gitCommit + branchMsg + ")";
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static java.lang.String getMinecraftVersion() {
        return Bukkit.getServer().getMinecraftVersion();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static void broadcast(net.md_5.bungee.api.chat.BaseComponent p0) {
        Bukkit.getServer().spigot().broadcast(p0);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static void broadcast(net.md_5.bungee.api.chat.BaseComponent... p0) {
        Bukkit.getServer().spigot().broadcast(p0);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static java.util.UUID getPlayerUniqueId(java.lang.String p0) {
        return Bukkit.getServer().getPlayerUniqueId(p0);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static boolean isTickingWorlds() {
        return Bukkit.getServer().isTickingWorlds();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static org.bukkit.World getWorld(org.bukkit.NamespacedKey p0) {
        return Bukkit.getServer().getWorld(p0);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static org.bukkit.inventory.ItemStack createExplorerMap(org.bukkit.World p0, org.bukkit.Location p1, org.bukkit.generator.structure.StructureType p2, org.bukkit.map.MapCursor.Type p3) {
        return Bukkit.getServer().createExplorerMap(p0, p1, p2, p3);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static org.bukkit.inventory.ItemStack createExplorerMap(org.bukkit.World p0, org.bukkit.Location p1, org.bukkit.generator.structure.StructureType p2, org.bukkit.map.MapCursor.Type p3, int p4, boolean p5) {
        return Bukkit.getServer().createExplorerMap(p0, p1, p2, p3, p4, p5);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static void updateResources() {
        Bukkit.getServer().updateResources();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static void updateRecipes() {
        Bukkit.getServer().updateRecipes();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static boolean addRecipe(org.bukkit.inventory.Recipe p0, boolean p1) {
        return Bukkit.getServer().addRecipe(p0, p1);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static boolean removeRecipe(org.bukkit.NamespacedKey p0, boolean p1) {
        return Bukkit.getServer().removeRecipe(p0, p1);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static int broadcast(net.kyori.adventure.text.Component p0) {
        return Bukkit.getServer().broadcast(p0);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static int broadcast(net.kyori.adventure.text.Component p0, java.lang.String p1) {
        return Bukkit.getServer().broadcast(p0, p1);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static org.bukkit.OfflinePlayer getOfflinePlayerIfCached(java.lang.String p0) {
        return Bukkit.getServer().getOfflinePlayerIfCached(p0);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static org.bukkit.command.CommandSender createCommandSender(java.util.function.Consumer<? super net.kyori.adventure.text.Component> p0) {
        return Bukkit.getServer().createCommandSender(p0);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder p0, org.bukkit.event.inventory.InventoryType p1, net.kyori.adventure.text.Component p2) {
        return Bukkit.getServer().createInventory(p0, p1, p2);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder p0, int p1, net.kyori.adventure.text.Component p2) throws java.lang.IllegalArgumentException {
        return Bukkit.getServer().createInventory(p0, p1, p2);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static org.bukkit.inventory.Merchant createMerchant(net.kyori.adventure.text.Component p0) {
        return Bukkit.getServer().createMerchant(p0);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static net.kyori.adventure.text.Component motd() {
        return Bukkit.getServer().motd();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static void motd(net.kyori.adventure.text.Component p0) {
        Bukkit.getServer().motd(p0);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static net.kyori.adventure.text.Component shutdownMessage() {
        return Bukkit.getServer().shutdownMessage();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static org.bukkit.generator.ChunkGenerator.ChunkData createVanillaChunkData(org.bukkit.World p0, int p1, int p2) {
        return Bukkit.getServer().createVanillaChunkData(p0, p1, p2);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static double[] getTPS() {
        return Bukkit.getServer().getTPS();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static long[] getTickTimes() {
        return Bukkit.getServer().getTickTimes();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static double getAverageTickTime() {
        return Bukkit.getServer().getAverageTickTime();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static org.bukkit.command.CommandMap getCommandMap() {
        return Bukkit.getServer().getCommandMap();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static void reloadPermissions() {
        Bukkit.getServer().reloadPermissions();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static boolean reloadCommandAliases() {
        return Bukkit.getServer().reloadCommandAliases();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static boolean suggestPlayerNamesWhenNullTabCompletions() {
        return Bukkit.getServer().suggestPlayerNamesWhenNullTabCompletions();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static java.lang.String getPermissionMessage() {
        return Bukkit.getServer().getPermissionMessage();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static net.kyori.adventure.text.Component permissionMessage() {
        return Bukkit.getServer().permissionMessage();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static com.destroystokyo.paper.profile.PlayerProfile createProfile(java.util.UUID p0) {
        return Bukkit.getServer().createProfile(p0);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static com.destroystokyo.paper.profile.PlayerProfile createProfile(java.lang.String p0) {
        return Bukkit.getServer().createProfile(p0);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static com.destroystokyo.paper.profile.PlayerProfile createProfile(java.util.UUID p0, java.lang.String p1) {
        return Bukkit.getServer().createProfile(p0, p1);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static com.destroystokyo.paper.profile.PlayerProfile createProfileExact(java.util.UUID p0, java.lang.String p1) {
        return Bukkit.getServer().createProfileExact(p0, p1);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static int getCurrentTick() {
        return Bukkit.getServer().getCurrentTick();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static boolean isStopping() {
        return Bukkit.getServer().isStopping();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static com.destroystokyo.paper.entity.ai.MobGoals getMobGoals() {
        return Bukkit.getServer().getMobGoals();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static io.papermc.paper.datapack.DatapackManager getDatapackManager() {
        return Bukkit.getServer().getDatapackManager();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static org.bukkit.potion.PotionBrewer getPotionBrewer() {
        return Bukkit.getServer().getPotionBrewer();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static io.papermc.paper.threadedregions.scheduler.RegionScheduler getRegionScheduler() {
        return Bukkit.getServer().getRegionScheduler();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static io.papermc.paper.threadedregions.scheduler.AsyncScheduler getAsyncScheduler() {
        return Bukkit.getServer().getAsyncScheduler();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler getGlobalRegionScheduler() {
        return Bukkit.getServer().getGlobalRegionScheduler();
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static boolean isOwnedByCurrentRegion(org.bukkit.World p0, io.papermc.paper.math.Position p1) {
        return Bukkit.getServer().isOwnedByCurrentRegion(p0, p1);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static boolean isOwnedByCurrentRegion(org.bukkit.World p0, io.papermc.paper.math.Position p1, int p2) {
        return Bukkit.getServer().isOwnedByCurrentRegion(p0, p1, p2);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static boolean isOwnedByCurrentRegion(org.bukkit.Location p0) {
        return Bukkit.getServer().isOwnedByCurrentRegion(p0);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static boolean isOwnedByCurrentRegion(org.bukkit.Location p0, int p1) {
        return Bukkit.getServer().isOwnedByCurrentRegion(p0, p1);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static boolean isOwnedByCurrentRegion(org.bukkit.block.Block p0) {
        return Bukkit.getServer().isOwnedByCurrentRegion(p0);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static boolean isOwnedByCurrentRegion(org.bukkit.World p0, int p1, int p2) {
        return Bukkit.getServer().isOwnedByCurrentRegion(p0, p1, p2);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static boolean isOwnedByCurrentRegion(org.bukkit.World p0, int p1, int p2, int p3) {
        return Bukkit.getServer().isOwnedByCurrentRegion(p0, p1, p2, p3);
    }
    @Unique
    @Widen(because = "paper-api: org.bukkit.Bukkit 的 public static 门面方法")
    private static boolean isOwnedByCurrentRegion(org.bukkit.entity.Entity p0) {
        return Bukkit.getServer().isOwnedByCurrentRegion(p0);
    }

}
