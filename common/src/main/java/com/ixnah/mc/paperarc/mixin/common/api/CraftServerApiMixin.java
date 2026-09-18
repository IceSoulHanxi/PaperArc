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
import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.CraftWorld;
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

/**
 * Paper API 方法补齐（合并原 Part1/Part2）：org.bukkit.craftbukkit.v.entity.CraftServer。
 * 因跨方法共享补充字段（permissionMessage/pluginsFolder 等），两 Part 已合并为单一 mixin 类。
 */
@Mixin(CraftServer.class)
public abstract class CraftServerApiMixin {

    // ---- Paper 补充字段（注入 CraftServer，字段名对齐 Paper patch 无前缀）----

    /** Classic Bukkit default, matching bukkit.yml settings.permission-message. */
    @Unique
    private static final String PAPERARC_DEFAULT_PERMISSION_MESSAGE =
            "\u00A7cI'm sorry, but you do not have permission to perform this command."
                    + " Please contact the server administrators if you believe that this is in error.";

    @Unique
    private String permissionMessage = PAPERARC_DEFAULT_PERMISSION_MESSAGE;

    @Unique
    private java.io.File pluginsFolder;

    @Unique
    private io.papermc.paper.threadedregions.scheduler.AsyncScheduler asyncScheduler;

    @Unique
    private io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler globalRegionScheduler;

    @Unique
    private io.papermc.paper.threadedregions.scheduler.RegionScheduler regionScheduler;

    @Unique
    private com.destroystokyo.paper.entity.ai.MobGoals mobGoals;

    @Unique
    private org.bukkit.potion.PotionBrewer potionBrewer;

    @Unique
    private net.kyori.adventure.text.Component shutdownMessage;

    @Unique
    private boolean suggestPlayerNamesWhenNullTabCompletions = true;

    @Unique
    private boolean isStopping;


    @Shadow
    public abstract net.minecraft.server.dedicated.DedicatedServer getServer();

    @Shadow
    public abstract net.minecraft.server.dedicated.DedicatedPlayerList getHandle();

    @Shadow
    private org.bukkit.configuration.file.YamlConfiguration commandsConfiguration;

    @Shadow
    private java.io.File getCommandsConfigFile() {
        throw new AssertionError();
    }

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
        // 1.20.1 MinecraftServer exposes float getAverageTickTime() (ms);
        // Paper's getAverageTickTimeNanos() is a CraftServer-only addition absent here.
        return this.getServer().getAverageTickTime();
    }

    /**
     * Paper maintains its own per-server tick counter; vanilla NMS keeps the
     * equivalent total in the private {@code MinecraftServer.tickCount} field,
     * widened via AT (f_129766_) and read directly — no reflection.
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
        return this.permissionMessage;
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
     * Vanilla NMS has no plugins-folder storage; default {@code ./plugins}
     * until a bootstrap layer records the real launch directory here.
     */
    @Unique
    public File getPluginsFolder() {
        return this.pluginsFolder == null ? new File("plugins") : this.pluginsFolder;
    }

    // ===== batch blocked-1 additions (scheduler / command-sender / explorer-map / mob-goals) =====

    /**
     * Sync-fallback: tasks run on Bukkit's shared async worker pool, not on
     * dedicated Folia threads — NOT truly asynchronous.
     */
    @Unique
    public io.papermc.paper.threadedregions.scheduler.AsyncScheduler getAsyncScheduler() {
        if (this.asyncScheduler == null) {
            this.asyncScheduler = new SimpleAsyncScheduler();
        }
        return this.asyncScheduler;
    }

    /**
     * Sync-fallback: every task runs on the main server thread; there is no
     * Folia global region thread — NOT truly asynchronous.
     */
    @Unique
    public io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler getGlobalRegionScheduler() {
        if (this.globalRegionScheduler == null) {
            this.globalRegionScheduler = new SimpleGlobalRegionScheduler();
        }
        return this.globalRegionScheduler;
    }

    // ===== A5-3：pairing 基线里的 NO_IMPL =====

    @Unique
    public io.papermc.paper.threadedregions.scheduler.RegionScheduler getRegionScheduler() {
        if (this.regionScheduler == null) {
            this.regionScheduler = new com.ixnah.mc.paperarc.bridge.scheduler.SimpleRegionScheduler();
        }
        return this.regionScheduler;
    }

    /** paper {@code Server#updateResources()}：重载数据包并把结果推给所有在线玩家。 */
    @Unique
    public void updateResources() {
        getHandle().reloadResources();
    }

    /**
     * paper {@code Server#updateRecipes()}：Paper 走的是自己加的
     * {@code PlayerList#reloadRecipeData()}，Arclight 没有（javap 核对），
     * 这里直接补发 vanilla 的 {@code ClientboundUpdateRecipesPacket}，效果等价。
     */
    @Unique
    public void updateRecipes() {
        net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket packet =
                new net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket(
                        getServer().getRecipeManager().getRecipes());
        for (net.minecraft.server.level.ServerPlayer player : getHandle().getPlayers()) {
            player.connection.send(packet);
        }
    }

    @Unique
    public boolean removeRecipe(org.bukkit.NamespacedKey key, boolean resendRecipes) {
        boolean removed = ((org.bukkit.Server) (Object) this).removeRecipe(key);
        if (removed && resendRecipes) {
            this.updateRecipes();
        }
        return removed;
    }

    /** paper {@code Server#reloadCommandAliases()}：逐行照抄 Paper 的实现。 */
    @Unique
    public boolean reloadCommandAliases() {
        org.bukkit.craftbukkit.v.CraftServer self = (org.bukkit.craftbukkit.v.CraftServer) (Object) this;
        java.util.Set<String> removals = self.getCommandAliases().keySet().stream()
                .map(key -> key.toLowerCase(java.util.Locale.ENGLISH))
                .collect(java.util.stream.Collectors.toSet());
        self.getCommandMap().getKnownCommands().keySet().removeIf(removals::contains);
        java.io.File file = this.getCommandsConfigFile();
        try {
            this.commandsConfiguration.load(file);
        } catch (java.io.FileNotFoundException ex) {
            return false;
        } catch (java.io.IOException | org.bukkit.configuration.InvalidConfigurationException ex) {
            org.bukkit.Bukkit.getLogger().log(java.util.logging.Level.SEVERE, "Cannot load " + file, ex);
            return false;
        }
        self.getCommandMap().registerServerAliases();
        return true;
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
        // 实现体在 bridge/：写成 mixin 里的 lambda 会让 invokedynamic 的引导方法
        // 引用 mixin 类自身，合并进 CraftServer 后描述符指向 mixin 包内的类
        // （checklist §1.6 c/d）。
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
        ResourceLocation structureId = ResourceLocation.tryParse(structureType.getKey().toString());
        if (structureId == null) {
            return null;
        }
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
        MapItemSavedData.addTargetDecoration(nmsMap, target, "+",
                net.minecraft.world.level.saveddata.maps.MapDecoration.Type.byIcon(mapIcon.getValue()));
        return CraftItemStack.asBukkitCopy(nmsMap);
    }

    /**
     * Simple MobGoals implementation tracking only goals added through this
     * manager (vanilla NMS goals are not wrapped); all API-added goals go to
     * the movement goal selector regardless of GoalType.
     */
    @Unique
    public com.destroystokyo.paper.entity.ai.MobGoals getMobGoals() {
        if (this.mobGoals == null) {
            this.mobGoals = new SimpleMobGoals();
        }
        return this.mobGoals;
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

    // ---- createVanillaChunkData (Allow-delegation-to-vanilla-chunk-gen.patch) ----
    @Unique
    @Deprecated
    public org.bukkit.generator.ChunkGenerator.ChunkData createVanillaChunkData(org.bukkit.World world, int x, int z) {
        // 完整实现需 ProtoChunk + ChunkStatus 生成管线（ProcessorMailbox/EmptyLevelChunk
        // /OldCraftChunkData.setRawChunkData）。Arclight 无对应基建且插件极少调用，
        // 这里退化为委托 createChunkData(World) 返回空 ChunkData，避免返回 null 引发 NPE。
        return ((CraftServer) (Object) this).createChunkData(world);
    }

    /**
     * Spigot 给 {@code MinecraftServer} 加的 {@code recentTps}（1m/5m/15m 平均值）。
     * 这是 CraftBukkit/Spigot 侧成员、<b>不参与 srg 重映射</b>，运行时名字就是 recentTps，
     * 因此按字面名 privateLookupIn 是正确的（vanilla NMS 成员则必须走 AT）。
     * 解析不到时 getTPS() 退化为 {20,20,20}。
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
        // Spigot-added MinecraftServer.recentTps keeps 1m/5m/15m averages; the
        // field is spigot-patched and absent from the vanilla mojmap jar, so it
        // is read through a MethodHandle.
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
        // MinecraftServer.tickTimes 是 vanilla 的 public final long[]（最近 tick 耗时环形缓冲，
        // 纳秒）——直访即可；按名字反射会在 srg 运行时（f_129748_）失败。
        long[] ticks = this.getServer().tickTimes;
        return ticks == null ? new long[0] : ticks;
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
        return LegacyComponentSerializer.legacySection().deserialize(this.permissionMessage);
    }

    @Unique
    public Component shutdownMessage() {
        // Paper keeps an optional shutdown broadcast; nullable by design.
        return this.shutdownMessage;
    }

    @Unique
    public boolean suggestPlayerNamesWhenNullTabCompletions() {
        // Paper config option; vanilla behaviour (suggest names) as default.
        return this.suggestPlayerNamesWhenNullTabCompletions;
    }

    @Unique
    public boolean isStopping() {
        // Paper flips this flag at the start of stopServer(); a bootstrap layer
        // may set it via the injected field.
        return this.isStopping;
    }

    @Unique
    public boolean isTickingWorlds() {
        // Approximation of Paper's flag via MinecraftServer#isStopped(): worlds
        // stay loaded/ticking until the server has fully stopped.
        return !this.getServer().isStopped();
    }

    @Unique
    public World getWorld(net.kyori.adventure.key.Key worldKey) {
        // Vanilla worlds live under the minecraft namespace; non-minecraft
        // namespaces fall back to a case-insensitive name scan.
        if ("minecraft".equals(worldKey.namespace())) {
            return ((CraftServer) (Object) this).getWorld(worldKey.value());
        }
        for (World world : ((org.bukkit.Server) (Object) this).getWorlds()) {
            if (world.getName().equalsIgnoreCase(worldKey.value())) {
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
        if (this.potionBrewer == null) {
            this.potionBrewer = new com.ixnah.mc.paperarc.bridge.PaperarcPotionBrewer();
        }
        return this.potionBrewer;
    }


    @Unique
    public void reloadPermissions() {
        // Paper reloads its PermissionsConfig then forces recalcs; here we
        // re-run CraftBukkit's private loadCustomPermissions() reflectively
        // and recalculate every online player's effective permissions.
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
     * Bridge for the {@link org.bukkit.Server#getCommandMap()} declared on the
     * runtime {@code org.bukkit.Server} interface by {@code ServerIfaceMixin}.
     * Arclight's {@code CraftServer} only has {@code SimpleCommandMap getCommandMap()}
     * (compiled without the interface method, so no covariant bridge was generated);
     * this mixin body supplies the {@code CommandMap}-typed bridge so
     * {@code invokeinterface} on the augmented interface resolves.
     */
    @Unique
    public org.bukkit.command.CommandMap getCommandMap() {
        return ((org.bukkit.craftbukkit.v.CraftServer) (Object) this).getCommandMap();
    }
    /**
     * {@code ForwardingAudience#audiences()}（A4-3 父接口差集）：与 Paper 一致，
     * 在线玩家 + 控制台。
     */
    @Unique
    public Iterable<? extends net.kyori.adventure.audience.Audience> audiences() {
        org.bukkit.Server server = (org.bukkit.Server) (Object) this;
        java.util.List<net.kyori.adventure.audience.Audience> out =
                new java.util.ArrayList<>(server.getOnlinePlayers());
        out.add(server.getConsoleSender());
        return out;
    }

}
