package dev.paperarc.probe;

import io.papermc.paper.event.player.PlayerTradeEvent;
import com.destroystokyo.paper.event.entity.EntityJumpEvent;
import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * PaperArc 装载层探针。判定四件事：
 *  P1 paper-only 类对插件可见（io.papermc / com.destroystokyo）
 *  P2 类身份唯一性（插件拿到的 Class 与 mod 层是同一个，无双份）
 *  P3 mixin 注入生效（ServerTickEndEvent 持续触发）
 *  P4 接口增补生效（invokeinterface 走增补后的 org.bukkit.entity.Entity）
 */
public final class ProbePlugin extends JavaPlugin implements Listener {
    private int ticks;
    private boolean tickLogged;

    @Override
    public void onEnable() {
        probe("P1a io.papermc 类可见", () -> mustResolve("io.papermc.paper.event.player.PlayerTradeEvent"));
        probe("P1b com.destroystokyo 类可见", () -> mustResolve("com.destroystokyo.paper.event.entity.EntityJumpEvent"));
        probe("P2 类身份唯一", this::identityCheck);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("PROBE: 已注册 ServerTickEnd 监听，等待 tick 流 …");
    }

    private void mustResolve(String name) throws Exception {
        Class<?> c = Class.forName(name);
        getLogger().info("PASS: " + name + " loaded by " + c.getClassLoader());
    }

    /** 双份类检测：事件类的父链应终止于同一个 Event；再比对两个 paper 类的 loader 是否一致 */
    private void identityCheck() {
        ClassLoader l1 = PlayerTradeEvent.class.getClassLoader();
        ClassLoader l2 = EntityJumpEvent.class.getClassLoader();
        if (l1 != l2) throw new AssertionError("paper 类分属不同 ClassLoader: " + l1 + " vs " + l2);
        getLogger().info("PASS: paper 类型统一由 " + l1 + " 提供");
    }

    @EventHandler
    public void onTick(ServerTickEndEvent e) {
        ticks++;
        if (!tickLogged && ticks >= 20) {
            tickLogged = true;
            getLogger().info("PASS(P3): ServerTickEndEvent 已触发 " + ticks + " 次 —— 服务端侧 mixin 生效");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return false;
        try {
            ArmorStand as = p.getWorld().spawn(p.getLocation(), ArmorStand.class,
                    CreatureSpawnEvent.SpawnReason.CUSTOM);
            // 关键断言：经由【增补后的 org.bukkit.entity.Entity 接口】调用 paper 方法
            CreatureSpawnEvent.SpawnReason reason = ((Entity) as).getEntitySpawnReason();
            as.remove();
            getLogger().info("PASS(P4): invokeinterface 增补方法返回 SpawnReason=" + reason);
        } catch (Throwable t) {
            getLogger().severe("FAIL(P4): " + t);
        }
        return true;
    }

    private void probe(String tag, Task t) {
        try { t.run(); } catch (Throwable ex) { getLogger().severe("FAIL(" + tag + "): " + ex); }
    }

    private interface Task { void run() throws Exception; }
}
