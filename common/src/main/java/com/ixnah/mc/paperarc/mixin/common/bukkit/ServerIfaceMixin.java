package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.StructureType;
import org.bukkit.World;
import com.destroystokyo.paper.entity.ai.MobGoals;
import io.papermc.paper.ban.BanListType;
import io.papermc.paper.datapack.DatapackManager;
import io.papermc.paper.math.Position;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityFactory;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.help.HelpMap;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemCraftResult;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.Recipe;
import org.bukkit.loot.LootTable;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapView;
import org.bukkit.packs.DataPackManager;
import org.bukkit.packs.ResourcePack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageRecipient;
import org.bukkit.potion.PotionBrewer;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.structure.StructureManager;
import org.bukkit.util.CachedServerIcon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.Server} (generated).
 * Adds 43 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.Server", remap = false)
public interface ServerIfaceMixin extends net.kyori.adventure.audience.ForwardingAudience {

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

    @Unique
    public abstract org.bukkit.inventory.Inventory createInventory(org.bukkit.inventory.InventoryHolder p0, org.bukkit.event.inventory.InventoryType p1, net.kyori.adventure.text.Component p2);

    @Unique
    public abstract boolean isOwnedByCurrentRegion(org.bukkit.Location p0);

    @Unique
    public abstract boolean isOwnedByCurrentRegion(org.bukkit.Location p0, int p1);

    @Unique
    public abstract boolean isOwnedByCurrentRegion(org.bukkit.World p0, int p1, int p2);

    @Unique
    public abstract org.bukkit.command.CommandMap getCommandMap();

    @Unique
    public default ItemStack createExplorerMap(World world, Location location, org.bukkit.generator.structure.StructureType structureType, MapCursor.Type mapIcon) {
        Server self = (Server) this;
        return self.createExplorerMap(world, location, structureType, mapIcon, 100, true);
    }

    /**
     * 下面四条是 paper 在 {@code Server} 上写成 default 的重载，运行时一条都没有
     * （B6-1 的 {@code checkApiDescriptors} 抓出来的：{@code Bukkit} 的同名静态门面
     * 一直在转发到不存在的方法，插件一调就 NoSuchMethodError）。方法体照抄 paper 的
     * default 实现（javap -c org/bukkit/Server.class）。
     */
    @Unique
    public default void broadcast(BaseComponent component) {
        ((Server) this).spigot().broadcast(component);
    }

    @Unique
    public default void broadcast(BaseComponent... components) {
        ((Server) this).spigot().broadcast(components);
    }

    @Unique
    public default World getWorld(org.bukkit.NamespacedKey worldKey) {
        return ((Server) this).getWorld((Key) worldKey);
    }

    @Unique
    public default boolean isOwnedByCurrentRegion(Block block) {
        return ((Server) this).isOwnedByCurrentRegion(block.getLocation());
    }
}
