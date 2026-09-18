package com.ixnah.mc.paperarc.bridge;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v.CraftServer;
import org.bukkit.event.Event;
import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.world.WorldBridge;

import java.lang.invoke.MethodHandles;

/**
 * Bridge helpers for calling CraftBukkit-injected methods on NMS classes.
 *
 * Methods like {@code Entity#getBukkitEntity()} or {@code Level#getWorld()} do not
 * exist on the vanilla (loom-provided) classes we compile against; they are added by
 * CraftBukkit at build time and only exist at runtime under Arclight. Static helpers
 * on Craft* classes are safe to call directly; the instance methods injected onto NMS
 * types are reached through Arclight's own bridge interfaces (compile-time stubs in
 * {@code common/src/arclightStub}), never through reflection or the static
 * {@code CraftEntity.getEntity} factory.
 *
 * <p><b>为什么不能用静态工厂</b>（checklist §1.10 an，A6/X-1 复核）：
 * {@code CraftEntity.getEntity(server, nms)} 每次都 new 一个新的 Craft 包装对象，
 * 而运行时真正的入口 {@code Entity#getBukkitEntity()} 会把结果缓存在 NMS
 * {@code Entity.bukkitEntity} 字段上。绕开缓存 → 同一实体拿到两个不同包装，
 * 插件的 {@code ==} 比较失败、挂在 Craft 类上的 {@code @Unique} 状态字段全部丢失。
 */
public final class PaperArcBridge {

    private PaperArcBridge() {
    }

    public static Server getServer() {
        return Bukkit.getServer();
    }

    /**
     * NMS entity -> Bukkit entity，走运行时缓存的 {@code getBukkitEntity()}。
     * 同一个 NMS 实体恒返回同一个 Craft 包装对象（见类注释）。
     */
    @SuppressWarnings("unchecked")
    public static <T extends org.bukkit.entity.Entity> T bukkitEntity(net.minecraft.world.entity.Entity nms) {
        return (T) ((EntityBridge) (Object) nms).bridge$getBukkitEntity();
    }

    public static org.bukkit.entity.Player bukkitPlayer(net.minecraft.world.entity.player.Player player) {
        return (org.bukkit.entity.Player) ((EntityBridge) (Object) player).bridge$getBukkitEntity();
    }

    /** ServerLevel -> CraftWorld，走运行时缓存的 {@code getWorld()}（每个 Level 一个）。 */
    public static org.bukkit.World bukkitWorld(ServerLevel level) {
        return ((WorldBridge) (Object) level).bridge$getWorld();
    }

    /** Fire a Bukkit event through the CraftBukkit plugin manager. */
    public static void fire(Event event) {
        ((CraftServer) getServer()).getPluginManager().callEvent(event);
    }

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
}
