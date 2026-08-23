package dev.paperarc.mixin.common.apiiface;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.Server} (generated).
 * Adds 43 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.Server", remap = false)
public interface ServerIfaceMixin {

    public abstract java.io.File getPluginsFolder();

    public abstract java.lang.String getMinecraftVersion();

    public abstract int broadcast(net.kyori.adventure.text.Component p0);

    public abstract java.util.UUID getPlayerUniqueId(java.lang.String p0);

    public abstract boolean isTickingWorlds();

    public abstract org.bukkit.World getWorld(net.kyori.adventure.key.Key p0);

    public abstract org.bukkit.inventory.ItemStack createExplorerMap(org.bukkit.World p0, org.bukkit.Location p1, org.bukkit.generator.structure.StructureType p2, org.bukkit.map.MapCursor.Type p3, int p4, boolean p5);

    public abstract void updateResources();

    public abstract void updateRecipes();

    public abstract boolean addRecipe(org.bukkit.inventory.Recipe p0, boolean p1);

    public abstract boolean removeRecipe(org.bukkit.NamespacedKey p0, boolean p1);

    public abstract int broadcast(net.kyori.adventure.text.Component p0, java.lang.String p1);

    public abstract org.bukkit.OfflinePlayer getOfflinePlayerIfCached(java.lang.String p0);

    public abstract org.bukkit.BanList getBanList(io.papermc.paper.ban.BanListType p0);

    public abstract org.bukkit.command.CommandSender createCommandSender(java.util.function.Consumer p0);

    public abstract org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder p0, org.bukkit.event.inventory.InventoryType p1, net.kyori.adventure.text.Component p2);

    public abstract org.bukkit.inventory.Merchant createMerchant(net.kyori.adventure.text.Component p0);

    public abstract net.kyori.adventure.text.Component motd();

    public abstract void motd(net.kyori.adventure.text.Component p0);

    public abstract net.kyori.adventure.text.Component shutdownMessage();

    public abstract double[] getTPS();

    public abstract long[] getTickTimes();

    public abstract double getAverageTickTime();

    public abstract void reloadPermissions();

    public abstract boolean reloadCommandAliases();

    public abstract boolean suggestPlayerNamesWhenNullTabCompletions();

    public abstract java.lang.String getPermissionMessage();

    public abstract net.kyori.adventure.text.Component permissionMessage();

    public abstract com.destroystokyo.paper.profile.PlayerProfile createProfile(java.util.UUID p0);

    public abstract com.destroystokyo.paper.profile.PlayerProfile createProfile(java.util.UUID p0, java.lang.String p1);

    public abstract com.destroystokyo.paper.profile.PlayerProfile createProfileExact(java.util.UUID p0, java.lang.String p1);

    public abstract int getCurrentTick();

    public abstract boolean isStopping();

    public abstract com.destroystokyo.paper.entity.ai.MobGoals getMobGoals();

    public abstract io.papermc.paper.datapack.DatapackManager getDatapackManager();

    public abstract org.bukkit.potion.PotionBrewer getPotionBrewer();

    public abstract io.papermc.paper.threadedregions.scheduler.RegionScheduler getRegionScheduler();

    public abstract io.papermc.paper.threadedregions.scheduler.AsyncScheduler getAsyncScheduler();

    public abstract io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler getGlobalRegionScheduler();

    public abstract boolean isOwnedByCurrentRegion(org.bukkit.World p0, io.papermc.paper.math.Position p1);

    public abstract boolean isOwnedByCurrentRegion(org.bukkit.World p0, int p1, int p2);

    public abstract boolean isOwnedByCurrentRegion(org.bukkit.Location p0);

    public abstract boolean isOwnedByCurrentRegion(org.bukkit.World p0, int p1, int p2, int p3);
}
