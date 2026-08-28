package com.ixnah.mc.paperarc.bridge;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.event.Event;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * Bridge helpers for calling CraftBukkit-injected methods on NMS classes.
 *
 * Methods like {@code Entity#getBukkitEntity()} or {@code ServerLevel#getWorld()}
 * do not exist on the vanilla (loom-provided) classes we compile against; they are
 * added by CraftBukkit at build time and only exist at runtime under Arclight.
 * Static helpers on Craft* classes are safe to call directly; instance methods
 * injected onto NMS types must go through reflection here.
 */
public final class PaperArcBridge {

    private PaperArcBridge() {
    }

    public static Server getServer() {
        return Bukkit.getServer();
    }

    /** NMS entity -> Bukkit entity via the static CraftBukkit factory. */
    @SuppressWarnings("unchecked")
    public static <T extends org.bukkit.entity.Entity> T bukkitEntity(net.minecraft.world.entity.Entity nms) {
        return (T) org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity.getEntity((CraftServer) getServer(), nms);
    }

    public static org.bukkit.entity.Player bukkitPlayer(net.minecraft.world.entity.player.Player player) {
        return (org.bukkit.entity.Player) org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity.getEntity((CraftServer) getServer(), player);
    }

    public static org.bukkit.World bukkitWorld(ServerLevel level) {
        try {
            Method getWorld = level.getClass().getMethod("getWorld");
            return (org.bukkit.World) getWorld.invoke(level);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("ServerLevel#getWorld is not available under this runtime", e);
        }
    }

    /** Fire a Bukkit event through the CraftBukkit plugin manager. */
    public static void fire(Event event) {
        ((CraftServer) getServer()).getPluginManager().callEvent(event);
    }

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
}
