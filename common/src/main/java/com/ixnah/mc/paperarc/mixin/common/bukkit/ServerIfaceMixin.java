package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.Server} (generated).
 * Adds 43 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.Server", remap = false)
public interface ServerIfaceMixin {

    @Unique
    public abstract java.io.File getPluginsFolder();

    @Unique
    public abstract java.lang.String getMinecraftVersion();

    @Unique
    public abstract int broadcast(net.kyori.adventure.text.Component p0);

    @Unique
    public abstract java.util.UUID getPlayerUniqueId(java.lang.String p0);

    @Unique
    public abstract boolean isTickingWorlds();

    @Unique
    public abstract org.bukkit.World getWorld(net.kyori.adventure.key.Key p0);

    @Unique
    public abstract org.bukkit.inventory.ItemStack createExplorerMap(org.bukkit.World p0, org.bukkit.Location p1, org.bukkit.generator.structure.StructureType p2, org.bukkit.map.MapCursor.Type p3, int p4, boolean p5);

    @Unique
    public abstract void updateResources();

    @Unique
    public abstract void updateRecipes();

    @Unique
    public abstract boolean addRecipe(org.bukkit.inventory.Recipe p0, boolean p1);

    @Unique
    public abstract boolean removeRecipe(org.bukkit.NamespacedKey p0, boolean p1);

    @Unique
    public abstract int broadcast(net.kyori.adventure.text.Component p0, java.lang.String p1);

    @Unique
    public abstract org.bukkit.OfflinePlayer getOfflinePlayerIfCached(java.lang.String p0);

    @Unique
    public abstract org.bukkit.BanList getBanList(io.papermc.paper.ban.BanListType p0);

    @Unique
    public abstract org.bukkit.command.CommandSender createCommandSender(java.util.function.Consumer p0);

    @Unique
    public abstract org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder p0, int p1, net.kyori.adventure.text.Component p2);

    @Unique
    public abstract org.bukkit.inventory.Merchant createMerchant(net.kyori.adventure.text.Component p0);

    @Unique
    public abstract net.kyori.adventure.text.Component motd();

    @Unique
    public abstract void motd(net.kyori.adventure.text.Component p0);

    @Unique
    public abstract net.kyori.adventure.text.Component shutdownMessage();

    @Unique
    public abstract double[] getTPS();

    @Unique
    public abstract long[] getTickTimes();

    @Unique
    public abstract double getAverageTickTime();

    @Unique
    public abstract void reloadPermissions();

    @Unique
    public abstract boolean reloadCommandAliases();

    @Unique
    public abstract boolean suggestPlayerNamesWhenNullTabCompletions();

    @Unique
    public abstract java.lang.String getPermissionMessage();

    @Unique
    public abstract net.kyori.adventure.text.Component permissionMessage();

    @Unique
    public abstract com.destroystokyo.paper.profile.PlayerProfile createProfile(java.util.UUID p0);

    @Unique
    public abstract com.destroystokyo.paper.profile.PlayerProfile createProfile(java.util.UUID p0, java.lang.String p1);

    @Unique
    public abstract com.destroystokyo.paper.profile.PlayerProfile createProfileExact(java.util.UUID p0, java.lang.String p1);

    @Unique
    public abstract com.destroystokyo.paper.profile.PlayerProfile createProfile(java.lang.String p0);

    @Unique
    public abstract com.destroystokyo.paper.profile.PlayerProfile createProfileExact(java.lang.String p1);

    @Unique
    public abstract int getCurrentTick();

    @Unique
    public abstract boolean isStopping();

    @Unique
    public abstract com.destroystokyo.paper.entity.ai.MobGoals getMobGoals();

    @Unique
    public abstract io.papermc.paper.datapack.DatapackManager getDatapackManager();

    @Unique
    public abstract org.bukkit.potion.PotionBrewer getPotionBrewer();

    @Unique
    public abstract io.papermc.paper.threadedregions.scheduler.RegionScheduler getRegionScheduler();

    @Unique
    public abstract io.papermc.paper.threadedregions.scheduler.AsyncScheduler getAsyncScheduler();

    @Unique
    public abstract io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler getGlobalRegionScheduler();

    @Unique
    public abstract boolean isOwnedByCurrentRegion(org.bukkit.World p0, io.papermc.paper.math.Position p1);

    @Unique
    public abstract boolean isOwnedByCurrentRegion(org.bukkit.World p0, io.papermc.paper.math.Position p1, int p2);

    @Unique
    public abstract boolean isOwnedByCurrentRegion(org.bukkit.entity.Entity p0);

    @Unique
    public abstract boolean isOwnedByCurrentRegion(org.bukkit.World p0, int p1, int p2, int p3);
}
