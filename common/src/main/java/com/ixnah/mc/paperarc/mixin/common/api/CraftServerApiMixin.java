package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import com.ixnah.mc.paperarc.bridge.CraftPlayerProfile;
import com.ixnah.mc.paperarc.bridge.api.SimpleMobGoals;
import com.ixnah.mc.paperarc.bridge.scheduler.SimpleAsyncScheduler;
import com.ixnah.mc.paperarc.bridge.scheduler.SimpleGlobalRegionScheduler;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import io.papermc.paper.math.Position;
import io.papermc.paper.potion.PotionMix;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
import org.bukkit.BanList;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.bukkit.craftbukkit.v.ban.CraftIpBanList;
import org.bukkit.craftbukkit.v.ban.CraftProfileBanList;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.generator.structure.StructureType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.Recipe;
import org.bukkit.map.MapCursor;
import org.bukkit.potion.PotionBrewer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * CraftServer 的 paper-api 扩展（原 batch B26 + B27 两个切片，2026-09-17 合并为一个
 * mixin —— 拆成 Part1/Part2 只是当初为了并行写代码，运行时两半要共享 @Unique 字段与
 * @Shadow 声明，分开反而重复）。
 *
 * Adds Paper-only {@link org.bukkit.Server} members missing from this
 * codebase's vanilla-based CraftServer.
 *
 * <p>Scheduler, mob-goals, command-sender and explorer-map members are
 * provided as sync-fallback implementations (see
 * docs/reports/blocked-batch1.md): everything routes through the classic
 * Bukkit main-thread scheduler and is NOT truly asynchronous. Methods relying
 * on Folia region schedulers, Paper permission/alias config infrastructure or
 * datapack resource reloading are intentionally left out and reported as
 * BLOCKED in docs/reports/api-B27.md.</p>
 */
@Mixin(CraftServer.class)
public abstract class CraftServerApiMixin {

    /** Paper 侧补充状态（原 ApiState 副表键 "paperarc:permissionMessage"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private String paperarc$permissionMessage;

    /** Paper 侧补充状态（原 ApiState 副表键 "paperarc:pluginsFolder"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private File paperarc$pluginsFolder;

    /** Paper 侧补充状态（原 ApiState 副表键 "paperarc:asyncScheduler"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private io.papermc.paper.threadedregions.scheduler.AsyncScheduler paperarc$asyncScheduler;

    /** Paper 侧补充状态（原 ApiState 副表键 "paperarc:globalRegionScheduler"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler paperarc$globalRegionScheduler;

    /** Paper 侧补充状态（原 ApiState 副表键 "paperarc:mobGoals"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private com.destroystokyo.paper.entity.ai.MobGoals paperarc$mobGoals;

    /** Paper 侧补充状态（原 ApiState 副表键 "paperarc:shutdownMessage"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private Component paperarc$shutdownMessage;

    /** Paper 侧补充状态（原 ApiState 副表键 "paperarc:suggestPlayerNamesWhenNullTabCompletions"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private Boolean paperarc$suggestPlayerNamesWhenNullTabCompletions;

    /** Paper 侧补充状态（原 ApiState 副表键 "paperarc:isStopping"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private Boolean paperarc$isStopping;

    /** Paper 侧补充状态（原 ApiState 副表键 "paperarc:potionBrewer"）；null = 未设置，读取时回落默认值。 */
    @Unique
    private PotionBrewer paperarc$potionBrewer;



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

    /**
     * Paper maintains its own per-server tick counter; vanilla NMS keeps the
     * equivalent total in the private {@code MinecraftServer.tickCount} field,
     * opened by {@code paperarc.accesswidener}.
     */
    @Unique
    public int getCurrentTick() {
        return this.getServer().tickCount;
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
        return (this.paperarc$permissionMessage != null ? this.paperarc$permissionMessage : (PAPERARC_DEFAULT_PERMISSION_MESSAGE));
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
        return (this.paperarc$pluginsFolder != null ? this.paperarc$pluginsFolder : (new File("plugins")));
    }

    // ===== batch blocked-1 additions (scheduler / command-sender / explorer-map / mob-goals) =====




    /**
     * Sync-fallback: tasks run on Bukkit's shared async worker pool, not on
     * dedicated Folia threads — NOT truly asynchronous.
     */
    @Unique
    public io.papermc.paper.threadedregions.scheduler.AsyncScheduler getAsyncScheduler() {
        io.papermc.paper.threadedregions.scheduler.AsyncScheduler scheduler =
                (this.paperarc$asyncScheduler != null ? this.paperarc$asyncScheduler : (null));
        if (scheduler == null) {
            scheduler = new SimpleAsyncScheduler();
            this.paperarc$asyncScheduler = scheduler;
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
                (this.paperarc$globalRegionScheduler != null ? this.paperarc$globalRegionScheduler : (null));
        if (scheduler == null) {
            scheduler = new SimpleGlobalRegionScheduler();
            this.paperarc$globalRegionScheduler = scheduler;
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
        // 实现体在 bridge/：写成 mixin 里的 lambda 会让 invokedynamic 引导方法引用 mixin 类自身。
        return com.ixnah.mc.paperarc.bridge.PaperarcFeedbackCommandSender.create(console, feedback);
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
                (this.paperarc$mobGoals != null ? this.paperarc$mobGoals : (null));
        if (goals == null) {
            goals = new SimpleMobGoals();
            this.paperarc$mobGoals = goals;
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





    /**
     * {@code MinecraftServer.recentTps}（1m/5m/15m 平均值）由 Arclight 的
     * MinecraftServerMixin 注入，不是 vanilla 成员、不参与 Fabric intermediary 重映射，
     * 按 B2-1 的分类保留反射（编译期不可见，用 privateLookupIn 取）。
     */
    @Unique
    private static final MethodHandle PAPERARC$RECENT_TPS = paperarc$buildRecentTpsHandle();

    @Unique
    private static MethodHandle paperarc$buildRecentTpsHandle() {
        try {
            return MethodHandles.privateLookupIn(net.minecraft.server.MinecraftServer.class, MethodHandles.lookup())
                    .findGetter(net.minecraft.server.MinecraftServer.class, "recentTps", double[].class);
        } catch (ReflectiveOperationException e) {
            return null; // getTPS() degrades to {20, 20, 20}
        }
    }

    @Unique
    public double[] getTPS() {
        // recentTps 由 Arclight 注入（见上），编译期不可见 -> MethodHandle 读取。
        if (PAPERARC$RECENT_TPS == null) {
            return new double[]{20.0D, 20.0D, 20.0D};
        }
        try {
            return (double[]) PAPERARC$RECENT_TPS.invoke(this.getServer());
        } catch (Throwable t) {
            return new double[]{20.0D, 20.0D, 20.0D};
        }
    }

    @Unique
    public long[] getTickTimes() {
        // Spigot 的 MinecraftServer.tickTimes 在 1.21.1 上不存在（Arclight 也没注入），
        // 原先的 MethodHandle 恒为 null、这个 API 一直返回空数组。1.21.1 vanilla 自带
        // 等价的 tickTimesNanos 环形缓冲并有公开访问器，直接用它。
        return this.getServer().getTickTimesNanos();
    }

    @Unique
    public Component motd() {
        return LegacyComponentSerializer.legacySection().deserialize(((CraftServer) (Object) this).getMotd());
    }

    @Unique
    public void motd(Component motd) {
        ((CraftServer) (Object) this).setMotd(LegacyComponentSerializer.legacySection().serialize(motd));
    }

    @Unique
    public Component permissionMessage() {
        String legacy = (this.paperarc$permissionMessage != null ? this.paperarc$permissionMessage : (PAPERARC_DEFAULT_PERMISSION_MESSAGE));
        return LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public Component shutdownMessage() {
        // Paper keeps an optional shutdown broadcast; nullable by design.
        return (this.paperarc$shutdownMessage != null ? this.paperarc$shutdownMessage : (null));
    }

    @Unique
    public boolean suggestPlayerNamesWhenNullTabCompletions() {
        // Paper config option; vanilla behaviour (suggest names) as default.
        return (this.paperarc$suggestPlayerNamesWhenNullTabCompletions != null ? this.paperarc$suggestPlayerNamesWhenNullTabCompletions : (Boolean.TRUE));
    }

    @Unique
    public boolean isStopping() {
        // Paper flips this flag at the start of stopServer(); we expose the
        // side-map flag (default false) for a bootstrap layer to set.
        return (this.paperarc$isStopping != null ? this.paperarc$isStopping : (Boolean.FALSE));
    }

    @Unique
    public boolean isTickingWorlds() {
        // Approximation of Paper's flag via MinecraftServer#isStopped(): worlds
        // stay loaded/ticking until the server has fully stopped.
        return !this.getServer().isStopped();
    }

    /**
     * 与 Paper 一致：逐个比 {@code World#getKey()}。注意那是**维度的 ResourceKey**
     * （主世界是 {@code minecraft:overworld}），不是世界文件夹名 —— 原来按名字查恒返回 null
     * （checklist §1.12 bf，1.20.1 的 {@code 75da326} 同款；探针 P25 在 main 上实测复现）。
     *
     * <p>按 namespace/value 两个字符串比，而不是 {@code equals}：形参可能是任意
     * {@code Key} 实现（{@code KeyImpl}），而 {@code NamespacedKey#equals} 要求同类。
     */
    @Unique
    public World getWorld(net.kyori.adventure.key.Key worldKey) {
        for (World world : ((org.bukkit.Server) (Object) this).getWorlds()) {
            org.bukkit.NamespacedKey key = world.getKey();
            if (key != null && key.getNamespace().equals(worldKey.namespace())
                    && key.getKey().equals(worldKey.value())) {
                return world;
            }
        }
        return null;
    }

    /**
     * Single-region (non-Folia) semantics, mirroring Paper's non-Folia
     * behaviour: every position belongs to the main server thread.
     */
    @Unique
    private boolean paperarc$isOwnedByCurrentRegion() {
        return ((org.bukkit.Server) (Object) this).isPrimaryThread();
    }

    @Unique
    public boolean isOwnedByCurrentRegion(org.bukkit.Location location) {
        return this.paperarc$isOwnedByCurrentRegion();
    }

    @Unique
    public boolean isOwnedByCurrentRegion(org.bukkit.Location location, int squareRadiusChunks) {
        return this.paperarc$isOwnedByCurrentRegion();
    }

    @Unique
    public boolean isOwnedByCurrentRegion(org.bukkit.World world, int chunkX, int chunkZ) {
        return this.paperarc$isOwnedByCurrentRegion();
    }

    @Unique
    public boolean isOwnedByCurrentRegion(org.bukkit.World world, int chunkX, int chunkZ, int squareRadiusChunks) {
        return this.paperarc$isOwnedByCurrentRegion();
    }

    @Unique
    public boolean isOwnedByCurrentRegion(org.bukkit.World world, Position position) {
        return this.paperarc$isOwnedByCurrentRegion();
    }

    @Unique
    public boolean isOwnedByCurrentRegion(org.bukkit.World world, Position position, int squareRadiusChunks) {
        return this.paperarc$isOwnedByCurrentRegion();
    }

    @Unique
    public boolean isOwnedByCurrentRegion(org.bukkit.entity.Entity entity) {
        return this.paperarc$isOwnedByCurrentRegion();
    }

    @Unique
    public PotionBrewer getPotionBrewer() {
        PotionBrewer brewer = (this.paperarc$potionBrewer != null ? this.paperarc$potionBrewer : (null));
        if (brewer == null) {
            brewer = new com.ixnah.mc.paperarc.bridge.PaperarcPotionBrewer();
            this.paperarc$potionBrewer = brewer;
        }
        return brewer;
    }

    @Unique
    public void reloadPermissions() {
        // Paper reloads its PermissionsConfig then forces recalcs; here we
        // re-run CraftBukkit's private loadCustomPermissions() reflectively
        // and recalculate every online player's effective permissions.
        // 目标是 CraftBukkit 类的成员（三端类名/成员名一致、不参与重映射），
        // 按 B2-1 的分类保留反射。
        try {
            Method loadCustomPermissions = CraftServer.class.getDeclaredMethod("loadCustomPermissions");
            loadCustomPermissions.setAccessible(true);
            loadCustomPermissions.invoke(this);
        } catch (ReflectiveOperationException e) {
            return;
        }
        for (org.bukkit.entity.Player player : ((org.bukkit.Server) (Object) this).getOnlinePlayers()) {
            player.recalculatePermissions();
        }
    }

    /**
     * {@code org.bukkit.Server#getCommandMap()} 由 ServerIfaceMixin 声明在运行时接口上。
     * Arclight 的 CraftServer 只有 {@code SimpleCommandMap getCommandMap()}——它编译时
     * 接口上没有这个方法，所以 javac 没生成协变桥；这里补出 {@code CommandMap} 返回类型的
     * 那一版，让增补接口上的 invokeinterface 能解析。
     */
    @Unique
    public org.bukkit.command.CommandMap getCommandMap() {
        return ((org.bukkit.craftbukkit.v.CraftServer) (Object) this).getCommandMap();
    }

    // ===== B2-4：IfaceMixin 早就声明、却一直没有实现体的 paper 方法 =====

    /** Paper 侧补充状态：RegionScheduler 的 sync-fallback 实例。 */
    @Unique
    private io.papermc.paper.threadedregions.scheduler.RegionScheduler paperarc$regionScheduler;

    @Unique
    public io.papermc.paper.threadedregions.scheduler.RegionScheduler getRegionScheduler() {
        io.papermc.paper.threadedregions.scheduler.RegionScheduler scheduler = this.paperarc$regionScheduler;
        if (scheduler == null) {
            scheduler = new com.ixnah.mc.paperarc.bridge.scheduler.SimpleRegionScheduler();
            this.paperarc$regionScheduler = scheduler;
        }
        return scheduler;
    }

    @Unique
    public boolean removeRecipe(NamespacedKey key, boolean resendRecipes) {
        boolean removed = ((CraftServer) (Object) this).removeRecipe(key);
        if (removed && resendRecipes) {
            this.updateRecipes();
        }
        return removed;
    }

    @Unique
    public void updateRecipes() {
        net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket packet =
                new net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket(
                        this.getServer().getRecipeManager().getRecipes());
        for (net.minecraft.server.level.ServerPlayer player : this.getHandle().getPlayers()) {
            if (player.connection != null) {
                player.connection.send(packet);
            }
        }
    }

    @Unique
    public void updateResources() {
        // Paper：重新下发标签 + 配方 + 命令树，让客户端跟上数据包的变化。
        net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket tags =
                new net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket(
                        net.minecraft.tags.TagNetworkSerialization
                                .serializeTagsToNetwork(this.getServer().registries()));
        for (net.minecraft.server.level.ServerPlayer player : this.getHandle().getPlayers()) {
            if (player.connection != null) {
                player.connection.send(tags);
            }
        }
        this.updateRecipes();
        ((CraftServer) (Object) this).syncCommands();
    }

    @Unique
    public boolean reloadCommandAliases() {
        CraftServer server = (CraftServer) (Object) this;
        java.util.Set<String> removals = new java.util.HashSet<>();
        java.util.Map<String, org.bukkit.command.Command> known =
                server.getCommandMap().getKnownCommands();
        for (java.util.Map.Entry<String, org.bukkit.command.Command> entry : known.entrySet()) {
            if (entry.getValue() instanceof org.bukkit.command.FormattedCommandAlias) {
                removals.add(entry.getKey());
            }
        }
        removals.forEach(known::remove);
        try {
            server.getCommandMap().registerServerAliases();
            return true;
        } catch (Exception e) {
            // 与 Paper 一致：commands.yml 有问题时返回 false 而不是抛出去
            server.getLogger().log(java.util.logging.Level.WARNING,
                    "Failed to reload command aliases from commands.yml", e);
            return false;
        }
    }
}
