package dev.paperarc.mixin.common.api;

import com.mojang.authlib.GameProfile;
import dev.paperarc.bridge.ApiState;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.BanList;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.craftbukkit.v.ban.CraftIpBanList;
import org.bukkit.craftbukkit.v.ban.CraftProfileBanList;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Part 1 of the CraftServer paper-api extension slice (batch B26).
 *
 * Adds Paper-only {@link org.bukkit.Server} members missing from this
 * codebase's vanilla-based CraftServer. Methods whose Paper implementations
 * rely on infrastructure absent here (Folia schedulers, Paper plugin-profile
 * classes, datapack/mob-goal manager wrappers, command-sender wrappers,
 * structure-registry mapping) are intentionally left out and reported as
 * BLOCKED in docs/reports/api-B26.md.
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
}
