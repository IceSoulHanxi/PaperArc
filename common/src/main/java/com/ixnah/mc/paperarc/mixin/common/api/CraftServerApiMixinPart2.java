package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.ApiState;
import io.papermc.paper.math.Position;
import io.papermc.paper.potion.PotionMix;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.potion.PotionBrewer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Part 2 of the CraftServer paper-api extension slice (batch B27).
 *
 * Adds the remaining Paper-only {@link org.bukkit.Server} members missing from
 * this codebase's vanilla-based CraftServer. Methods relying on Folia region
 * schedulers, Paper permission/alias config infrastructure or datapack resource
 * reloading are intentionally left out and reported as BLOCKED in
 * docs/reports/api-B27.md.
 */
@Mixin(CraftServer.class)
public abstract class CraftServerApiMixinPart2 {

    @Unique
    private static final String PAPERARC_POTION_BREWER_KEY = "paperarc:potionBrewer";

    @Unique
    private static final String PAPERARC_SHUTDOWN_MESSAGE_KEY = "paperarc:shutdownMessage";

    @Unique
    private static final String PAPERARC_SUGGEST_PLAYER_NAMES_KEY =
            "paperarc:suggestPlayerNamesWhenNullTabCompletions";

    @Unique
    private static final String PAPERARC_IS_STOPPING_KEY = "paperarc:isStopping";

    /** Classic Bukkit default, matching bukkit.yml settings.permission-message. */
    @Unique
    private static final String PAPERARC_DEFAULT_PERMISSION_MESSAGE =
            "\u00A7cI'm sorry, but you do not have permission to perform this command."
                    + " Please contact the server administrators if you believe that this is in error.";

    /** Same storage key as CraftServerApiMixinPart1#getPermissionMessage. */
    @Unique
    private static final String PAPERARC_PERMISSION_MESSAGE_KEY = "paperarc:permissionMessage";

    @Shadow
    public abstract net.minecraft.server.dedicated.DedicatedServer getServer();

    /**
     * Spigot-patched {@code MinecraftServer.recentTps} (1m/5m/15m averages);
     * absent from the vanilla compile jar, so resolved through privateLookupIn.
     */
    @Unique
    private static final MethodHandle PAPERARC$RECENT_TPS = paperarc$buildRecentTpsHandle();

    /**
     * Spigot-patched {@code MinecraftServer.tickTimes} ring buffer. Not present
     * in current Arclight builds -> null, degrading getTickTimes() to an empty
     * array.
     */
    @Unique
    private static final MethodHandle PAPERARC$TICK_TIMES = paperarc$buildTickTimesHandle();

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
    private static MethodHandle paperarc$buildTickTimesHandle() {
        try {
            return MethodHandles.privateLookupIn(net.minecraft.server.MinecraftServer.class, MethodHandles.lookup())
                    .findGetter(net.minecraft.server.MinecraftServer.class, "tickTimes", long[].class);
        } catch (ReflectiveOperationException e) {
            return null; // getTickTimes() degrades to an empty array
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
        // Spigot MinecraftServer.tickTimes ring buffer of the last tick
        // durations in nanoseconds; read through a MethodHandle like getTPS.
        if (PAPERARC$TICK_TIMES == null) {
            return new long[0];
        }
        try {
            Object value = PAPERARC$TICK_TIMES.invoke(this.getServer());
            if (value instanceof long[] ticks) {
                return ticks;
            }
            return new long[0];
        } catch (Throwable t) {
            return new long[0];
        }
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
        String legacy = ApiState.get(this, PAPERARC_PERMISSION_MESSAGE_KEY,
                PAPERARC_DEFAULT_PERMISSION_MESSAGE);
        return LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public Component shutdownMessage() {
        // Paper keeps an optional shutdown broadcast; nullable by design.
        return ApiState.get(this, PAPERARC_SHUTDOWN_MESSAGE_KEY, null);
    }

    @Unique
    public boolean suggestPlayerNamesWhenNullTabCompletions() {
        // Paper config option; vanilla behaviour (suggest names) as default.
        return ApiState.get(this, PAPERARC_SUGGEST_PLAYER_NAMES_KEY, Boolean.TRUE);
    }

    @Unique
    public boolean isStopping() {
        // Paper flips this flag at the start of stopServer(); we expose the
        // side-map flag (default false) for a bootstrap layer to set.
        return ApiState.get(this, PAPERARC_IS_STOPPING_KEY, Boolean.FALSE);
    }

    @Unique
    public boolean isTickingWorlds() {
        // Approximation of Paper's flag via MinecraftServer#isStopped(): worlds
        // stay loaded/ticking until the server has fully stopped.
        try {
            Object stopped = net.minecraft.server.MinecraftServer.class.getMethod("isStopped")
                    .invoke(this.getServer());
            return !((Boolean) stopped);
        } catch (ReflectiveOperationException e) {
            return true;
        }
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
        PotionBrewer brewer = ApiState.get(this, PAPERARC_POTION_BREWER_KEY, null);
        if (brewer == null) {
            brewer = new PaperarcPotionBrewer();
            ApiState.put(this, PAPERARC_POTION_BREWER_KEY, brewer);
        }
        return brewer;
    }

    /**
     * PotionBrewer delegating the vanilla lookup methods to the CraftBukkit
     * base {@link org.bukkit.craftbukkit.v1_20_R1.potion.CraftPotionBrewer} (present in
     * the deobf classpath) and keeping Paper's custom potion-mix extensions in an
     * in-memory side map (vanilla brewing registry untouched).
     */
    private static final class PaperarcPotionBrewer implements PotionBrewer {

        private static final org.bukkit.craftbukkit.v1_20_R1.potion.CraftPotionBrewer CB_BREWER = new org.bukkit.craftbukkit.v1_20_R1.potion.CraftPotionBrewer();
        private static final List<PotionMix> MIXES = new ArrayList<>();

        @Override
        public void addPotionMix(PotionMix mix) {
            synchronized (MIXES) {
                MIXES.removeIf(existing -> existing.getKey().equals(mix.getKey()));
                MIXES.add(mix);
            }
        }

        @Override
        public void removePotionMix(NamespacedKey key) {
            synchronized (MIXES) {
                MIXES.removeIf(existing -> existing.getKey().equals(key));
            }
        }

        @Override
        public void resetPotionMixes() {
            synchronized (MIXES) {
                MIXES.clear();
            }
        }

        @Override
        public Collection<PotionEffect> getEffects(PotionType type, boolean upgraded, boolean extended) {
            return CB_BREWER.getEffects(type, upgraded, extended);
        }

        @Override
        public Collection<PotionEffect> getEffectsFromDamage(int damage) {
            return CB_BREWER.getEffectsFromDamage(damage);
        }

        @Override
        public org.bukkit.potion.PotionEffect createEffect(org.bukkit.potion.PotionEffectType potion, int duration, int amplifier) {
            return CB_BREWER.createEffect(potion, duration, amplifier);
        }
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
}
