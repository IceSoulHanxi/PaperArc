package com.ixnah.mc.paperarc.bridge;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.event.Event;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
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

    /**
     * NMS entity -> Bukkit entity，**必须走 {@code Entity#getBukkitEntity()}**。
     *
     * <p>{@code CraftEntity.getEntity(server, nms)} 的字节码是一长串 {@code new}，
     * 不查缓存；而 Arclight 的 {@code EntityMixin.internal$getBukkitEntity()} 里有
     * {@code getfield bukkitEntity / ifnonnull / putfield} 的缓存字段。直接调工厂
     * 会让每个事件、每个 API 返回值都发一个新包装对象 —— 插件用 {@code ==} 比较失效，
     * 我们挂在 Craft 类上的 {@code @Unique} 状态字段也会随包装对象丢失
     * （checklist §1.10 an）。</p>
     *
     * <p>{@code getBukkitEntity} 是 Arclight/CraftBukkit 注入到 NMS 上的成员，
     * 不参与 intermediary/srg 重映射，按 B2-1 的分类属于"可保留反射"的一类，
     * 这里用一次性解析的 {@link MethodHandle} 承载。</p>
     */
    @SuppressWarnings("unchecked")
    public static <T extends org.bukkit.entity.Entity> T bukkitEntity(net.minecraft.world.entity.Entity nms) {
        if (nms == null) {
            return null;
        }
        if (GET_BUKKIT_ENTITY != null) {
            try {
                return (T) (org.bukkit.entity.Entity) GET_BUKKIT_ENTITY.invoke(nms);
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable t) {
                throw new IllegalStateException("Entity#getBukkitEntity() failed", t);
            }
        }
        if (GET_BUKKIT_ENTITY_METHOD != null) {
            try {
                return (T) GET_BUKKIT_ENTITY_METHOD.invoke(nms);
            } catch (Throwable ignored) {
            }
        }
        return (T) org.bukkit.craftbukkit.v.entity.CraftEntity.getEntity((CraftServer) getServer(), nms);
    }

    public static org.bukkit.entity.Player bukkitPlayer(net.minecraft.world.entity.player.Player player) {
        return (org.bukkit.entity.Player) bukkitEntity(player);
    }

    /** {@code Entity#getBukkitEntity()}；解析不到时退回反射/工厂。 */
    private static final MethodHandle GET_BUKKIT_ENTITY = buildBukkitEntityHandle();
    private static final Method GET_BUKKIT_ENTITY_METHOD = findBukkitEntityMethod();

    private static Method findBukkitEntityMethod() {
        try {
            Method m = Entity.class.getMethod("getBukkitEntity");
            m.setAccessible(true);
            return m;
        } catch (Throwable t) {
            return null;
        }
    }

    private static MethodHandle buildBukkitEntityHandle() {
        try {
            Method m = Entity.class.getMethod("getBukkitEntity");
            return MethodHandles.lookup().unreflect(m);
        } catch (Throwable e) {
            try {
                return MethodHandles.publicLookup().findVirtual(
                        Entity.class,
                        "getBukkitEntity",
                        MethodType.methodType(org.bukkit.craftbukkit.v.entity.CraftEntity.class));
            } catch (Throwable t) {
                return null;
            }
        }
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
