package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.ixnah.mc.paperarc.bridge.ApiState;
import com.ixnah.mc.paperarc.bridge.CraftPlayerProfile;
import com.ixnah.mc.paperarc.bridge.api.SimpleMobGoals;
import com.ixnah.mc.paperarc.bridge.scheduler.SimpleAsyncScheduler;
import com.ixnah.mc.paperarc.bridge.scheduler.SimpleGlobalRegionScheduler;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.BanList;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.bukkit.craftbukkit.v.ban.CraftIpBanList;
import org.bukkit.craftbukkit.v.ban.CraftProfileBanList;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.Recipe;
import org.bukkit.map.MapCursor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Part 1 of the CraftServer paper-api extension slice (batch B26).
 *
 * Adds Paper-only {@link org.bukkit.Server} members missing from this
 * codebase's vanilla-based CraftServer.
 *
 * <p>Scheduler, mob-goals, command-sender and explorer-map members are
 * provided as sync-fallback implementations (see
 * docs/reports/blocked-batch1.md): everything routes through the classic
 * Bukkit main-thread scheduler and is NOT truly asynchronous. The datapack
 * manager remains unimplemented (needs Datapack wrapper infrastructure).</p>
 */
@Mixin(CraftServer.class)
public abstract class CraftServerApiMixinPart1 {

    @Unique
    private static final String PAPERARC_PERMISSION_MSG_KEY = "paperarc:permissionMessage";

    @Unique
    private static final String PAPERARC_PLUGINS_FOLDER_KEY = "paperarc:pluginsFolder";

    /**
     * Classic Bukkit default, matching bukkit.yml settings.permission-message
     * before colour-code translation.
     */
    @Unique
    private static final String PAPERARC_DEFAULT_PERMISSION_MESSAGE =
            "\u00A7cI'm sorry, but you do not have permission to perform this command."
                    + " Please contact the server administrators if you believe that this is in error.";

    /** Lazily resolved MinecraftServer.tickCount (private in vanilla NMS). */
    private static volatile Field PAPERARC_TICK_COUNT_FIELD;

    @Shadow
    public abstract net.minecraft.server.dedicated.DedicatedServer getServer();

    @Shadow
    public abstract net.minecraft.server.dedicated.DedicatedPlayerList getHandle();

    @Unique
    public boolean addRecipe(org.bukkit.inventory.Recipe recipe, boolean resetRegistry) {
        // Paper reloads the recipe registry before adding when resetRegistry is
        // true; registry finalization needs lifecycle infra, so only the add
        // half is delegated to the vanilla CraftServer implementation.
        return ((CraftServer) (Object) this).addRecipe(recipe);
    }

    @Unique
    public int broadcast(Component message) {
        // Matches Bukkit's legacy broadcastMessage(String) semantics: all online
        // players holding bukkit.broadcast.user receive the component natively.
        int count = 0;
        for (org.bukkit.entity.Player player : ((org.bukkit.Server) (Object) this).getOnlinePlayers()) {
            if (player.hasPermission("bukkit.broadcast.user")) { // Server.BROADCAST_CHANNEL_USERS
                player.sendMessage(message);
                count++;
            }
        }
        return count;
    }

    @Unique
    public int broadcast(Component message, String permission) {
        int count = 0;
        for (org.bukkit.entity.Player player : ((org.bukkit.Server) (Object) this).getOnlinePlayers()) {
            if (permission == null || player.hasPermission(permission)) {
                player.sendMessage(message);
                count++;
            }
        }
        return count;
    }

    @Unique
    public org.bukkit.inventory.Inventory createInventory(InventoryHolder owner, int size, Component title) {
        String legacy = title == null ? null : LegacyComponentSerializer.legacySection().serialize(title);
        return ((CraftServer) (Object) this).createInventory(owner, size, legacy);
    }

    @Unique
    public org.bukkit.inventory.Inventory createInventory(InventoryHolder owner, InventoryType type, Component title) {
        String legacy = title == null ? null : LegacyComponentSerializer.legacySection().serialize(title);
        return ((CraftServer) (Object) this).createInventory(owner, type, legacy);
    }

    @Unique
    public Merchant createMerchant(Component title) {
        String legacy = title == null ? null : LegacyComponentSerializer.legacySection().serialize(title);
        return ((CraftServer) (Object) this).createMerchant(legacy);
    }

    @Unique
    public double getAverageTickTime() {
        return this.getServer().getAverageTickTimeNanos() / 1.0E6D;
    }

    @Unique
    public BanList getBanList(io.papermc.paper.ban.BanListType type) {
        if (io.papermc.paper.ban.BanListType.IP.equals(type)) {
            return new CraftIpBanList(this.getServer().getPlayerList().getIpBans());
        }
        return new CraftProfileBanList(this.getServer().getPlayerList().getBans());
    }

    @Unique
    private static Field paperarc$tickCountField() throws ReflectiveOperationException {
        Field field = PAPERARC_TICK_COUNT_FIELD;
        if (field == null) {
            field = net.minecraft.server.MinecraftServer.class.getDeclaredField("tickCount");
            field.setAccessible(true);
            PAPERARC_TICK_COUNT_FIELD = field;
        }
        return field;
    }

    /**
     * Paper maintains its own per-server tick counter; vanilla NMS keeps the
     * equivalent total in the private {@code MinecraftServer.tickCount} field,
     * read here reflectively (no public accessor exists).
     */
    @Unique
    public int getCurrentTick() {
        try {
            return paperarc$tickCountField().getInt(this.getServer());
        } catch (ReflectiveOperationException e) {
            return -1;
        }
    }

    @Unique
    public String getMinecraftVersion() {
        return SharedConstants.getCurrentVersion().getName();
    }

    @Unique
    public OfflinePlayer getOfflinePlayerIfCached(String name) {
        Optional<GameProfile> profile = this.getServer().getProfileCache().get(name);
        if (!profile.isPresent()) {
            return null;
        }
        // CraftServer.getOfflinePlayer(GameProfile) is public (javap-verified).
        return ((CraftServer) (Object) this).getOfflinePlayer(profile.get());
    }

    @Unique
    public String getPermissionMessage() {
        return ApiState.get(this, PAPERARC_PERMISSION_MSG_KEY, PAPERARC_DEFAULT_PERMISSION_MESSAGE);
    }

    @Unique
    public java.util.UUID getPlayerUniqueId(String name) {
        org.bukkit.entity.Player online = ((org.bukkit.Server) (Object) this).getPlayerExact(name);
        if (online != null) {
            return online.getUniqueId();
        }
        Optional<GameProfile> profile = this.getServer().getProfileCache().get(name);
        return profile.map(GameProfile::getId).orElse(null);
    }

    /**
     * Vanilla NMS has no plugins-folder storage; the location lives in the
     * ApiState side map (default {@code ./plugins}) until a bootstrap layer
     * records the real launch directory there.
     */
    @Unique
    public File getPluginsFolder() {
        return ApiState.get(this, PAPERARC_PLUGINS_FOLDER_KEY, new File("plugins"));
    }

    // ===== batch blocked-1 additions (scheduler / command-sender / explorer-map / mob-goals) =====

    @Unique
    private static final String PAPERARC_ASYNC_SCHEDULER_KEY = "paperarc:asyncScheduler";

    @Unique
    private static final String PAPERARC_GLOBAL_SCHEDULER_KEY = "paperarc:globalRegionScheduler";

    @Unique
    private static final String PAPERARC_MOB_GOALS_KEY = "paperarc:mobGoals";

    /**
     * Sync-fallback: tasks run on Bukkit's shared async worker pool, not on
     * dedicated Folia threads — NOT truly asynchronous.
     */
    @Unique
    public io.papermc.paper.threadedregions.scheduler.AsyncScheduler getAsyncScheduler() {
        io.papermc.paper.threadedregions.scheduler.AsyncScheduler scheduler =
                ApiState.get(this, PAPERARC_ASYNC_SCHEDULER_KEY, null);
        if (scheduler == null) {
            scheduler = new SimpleAsyncScheduler();
            ApiState.put(this, PAPERARC_ASYNC_SCHEDULER_KEY, scheduler);
        }
        return scheduler;
    }

    /**
     * Sync-fallback: every task runs on the main server thread; there is no
     * Folia global region thread — NOT truly asynchronous.
     */
    @Unique
    public io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler getGlobalRegionScheduler() {
        io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler scheduler =
                ApiState.get(this, PAPERARC_GLOBAL_SCHEDULER_KEY, null);
        if (scheduler == null) {
            scheduler = new SimpleGlobalRegionScheduler();
            ApiState.put(this, PAPERARC_GLOBAL_SCHEDULER_KEY, scheduler);
        }
        return scheduler;
    }

    /**
     * Sync-fallback command sender: an interface proxy over the real console
     * sender whose {@code sendMessage} overloads are redirected into the given
     * feedback consumer instead of the console. Non-message methods (and
     * exotic Audience overloads with chat-type bounds) delegate to the console
     * sender.
     */
    @Unique
    public org.bukkit.command.CommandSender createCommandSender(
            java.util.function.Consumer<? super Component> feedback) {
        Preconditions.checkArgument(feedback != null, "feedback cannot be null");
        org.bukkit.command.ConsoleCommandSender console =
                ((org.bukkit.Server) (Object) this).getConsoleSender();
        return (org.bukkit.command.CommandSender) java.lang.reflect.Proxy.newProxyInstance(
                CraftServerApiMixinPart1.class.getClassLoader(),
                new Class<?>[] {org.bukkit.command.ConsoleCommandSender.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("sendMessage".equals(name) && args != null && args.length >= 1) {
                        Object first = args[0];
                        if (first instanceof Component component) {
                            feedback.accept(component);
                            return null;
                        }
                        if (first instanceof net.kyori.adventure.text.ComponentLike like) {
                            feedback.accept(like.asComponent());
                            return null;
                        }
                        if (first instanceof String legacy) {
                            feedback.accept(LegacyComponentSerializer.legacySection().deserialize(legacy));
                            return null;
                        }
                        if (first instanceof String[] legacies) {
                            for (String line : legacies) {
                                feedback.accept(LegacyComponentSerializer.legacySection().deserialize(line));
                            }
                            return null;
                        }
                    } else if ("toString".equals(name)) {
                        return "PaperArcCommandSender";
                    }
                    try {
                        return method.invoke(console, args);
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        throw e.getCause() == null ? e : e.getCause();
                    }
                });
    }

    /**
     * Sync-fallback explorer map: locates the nearest structure through the
     * vanilla world generator ({@code findNearestMapStructure}, radius 100,
     * matching Paper), then builds a filled map via {@link MapItem#create}
     * + biome preview + target decoration, mirroring the vanilla cartographer
     * flow. Returns {@code null} when the structure is unknown to the registry
     * or cannot be located.
     */
    @Unique
    public ItemStack createExplorerMap(org.bukkit.World world, org.bukkit.Location location,
                                       StructureType structureType, MapCursor.Type mapIcon,
                                       int zoom, boolean unlimitedTracking) {
        Preconditions.checkArgument(world != null, "world cannot be null");
        Preconditions.checkArgument(location != null, "location cannot be null");
        Preconditions.checkArgument(structureType != null, "structureType cannot be null");
        Preconditions.checkArgument(mapIcon != null, "mapIcon cannot be null");
        if (!(world instanceof CraftWorld craftWorld)) {
            return null;
        }
        net.minecraft.server.level.ServerLevel level = craftWorld.getHandle();
        Registry<Structure> structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        ResourceLocation structureId = ResourceLocation.parse(structureType.getKey().toString());
        Holder<Structure> holder = structures
                .getHolder(ResourceKey.create(Registries.STRUCTURE, structureId))
                .orElse(null);
        if (holder == null) {
            return null;
        }
        BlockPos origin = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        Pair<BlockPos, Holder<Structure>> found = level.getChunkSource().getGenerator()
                .findNearestMapStructure(level, HolderSet.direct(holder), origin, 100, false);
        if (found == null) {
            return null;
        }
        BlockPos target = found.getFirst();
        net.minecraft.world.item.ItemStack nmsMap = MapItem.create(level,
                target.getX(), target.getZ(), (byte) zoom, true, unlimitedTracking);
        MapItem.renderBiomePreviewMap(level, nmsMap);
        Holder<MapDecorationType> icon = paperarc$mapDecorationType(level, mapIcon);
        if (icon != null) {
            MapItemSavedData.addTargetDecoration(nmsMap, target, "+", icon);
        }
        return CraftItemStack.asBukkitCopy(nmsMap);
    }

    /**
     * Resolves a {@link MapCursor.Type} against the vanilla map-decoration
     * registry by lowercased enum name; falls back to the classic red-X
     * ({@code minecraft:target_x}) marker used by vanilla explorer maps when
     * the requested icon has no direct NMS counterpart.
     */
    @Unique
    private static Holder<MapDecorationType> paperarc$mapDecorationType(
            net.minecraft.server.level.ServerLevel level, MapCursor.Type icon) {
        Registry<MapDecorationType> decorations =
                level.registryAccess().registryOrThrow(Registries.MAP_DECORATION_TYPE);
        Holder<MapDecorationType> resolved = decorations.getHolder(ResourceKey.create(
                        Registries.MAP_DECORATION_TYPE,
                        ResourceLocation.fromNamespaceAndPath("minecraft", icon.name().toLowerCase(Locale.ROOT))))
                .orElse(null);
        if (resolved != null) {
            return resolved;
        }
        return decorations.getHolder(ResourceKey.create(Registries.MAP_DECORATION_TYPE,
                ResourceLocation.withDefaultNamespace("target_x"))).orElse(null);
    }

    /**
     * Simple MobGoals implementation tracking only goals added through this
     * manager (vanilla NMS goals are not wrapped); all API-added goals go to
     * the movement goal selector regardless of GoalType.
     */
    @Unique
    public com.destroystokyo.paper.entity.ai.MobGoals getMobGoals() {
        com.destroystokyo.paper.entity.ai.MobGoals goals =
                ApiState.get(this, PAPERARC_MOB_GOALS_KEY, null);
        if (goals == null) {
            goals = new SimpleMobGoals();
            ApiState.put(this, PAPERARC_MOB_GOALS_KEY, goals);
        }
        return goals;
    }

    /**
     * Not implemented: {@code DatapackManager} needs io.papermc.paper.datapack
     * wrappers over the NMS PackRepository (Datapack/DatapackSet lifecycle),
     * which this vanilla-based build does not have. Thrown instead of returning
     * a broken wrapper — see docs/reports/blocked-batch1.md.
     */
    @Unique
    public io.papermc.paper.datapack.DatapackManager getDatapackManager() {
        throw new UnsupportedOperationException(
                "PaperArc: DatapackManager requires io.papermc.paper.datapack.Datapack wrappers over the "
                        + "NMS PackRepository, which are not present in this vanilla-based build; "
                        + "see docs/reports/blocked-batch1.md");
    }

    // ===== batch blocked-2 additions (Paper PlayerProfile factory methods) =====

    /** Wraps {@code (id, null)}; completion fills the name from the profile cache. */
    @Unique
    public com.destroystokyo.paper.profile.PlayerProfile createProfile(java.util.UUID uniqueId) {
        return new CraftPlayerProfile(new GameProfile(uniqueId, null));
    }

    @Unique
    public com.destroystokyo.paper.profile.PlayerProfile createProfile(java.util.UUID uniqueId, String name) {
        return new CraftPlayerProfile(new GameProfile(uniqueId, name));
    }

    /** No cached id available yet; a random UUID is used as a placeholder (Paper parity). */
    @Unique
    public com.destroystokyo.paper.profile.PlayerProfile createProfile(String name) {
        return new CraftPlayerProfile(new GameProfile(java.util.UUID.randomUUID(), name));
    }

    /** Exact variant: stores both values verbatim without offline-UUID derivation. */
    @Unique
    public com.destroystokyo.paper.profile.PlayerProfile createProfileExact(java.util.UUID uniqueId, String name) {
        return new CraftPlayerProfile(new GameProfile(uniqueId, name));
    }

    /** Exact variant: name only, no placeholder UUID derivation. */
    @Unique
    public com.destroystokyo.paper.profile.PlayerProfile createProfileExact(String name) {
        return new CraftPlayerProfile(new GameProfile(null, name));
    }
}
