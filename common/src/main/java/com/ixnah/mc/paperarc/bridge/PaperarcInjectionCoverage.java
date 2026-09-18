package com.ixnah.mc.paperarc.bridge;

import java.util.ArrayList;
import java.util.List;

/**
 * 把"本轮所有新增注入的目标类"显式拉进类加载，逼 mixin 真的对它们应用一次。
 *
 * <p><b>为什么需要</b>（1.20.1 的 A7 发现，checklist §2.8 bi）：只在玩家登录/特定路径才用到的类
 * （{@code ServerCommonPacketListenerImpl}、{@code KickCommand}、{@code NetherPortalBlock}…）
 * 在一台没人上线的验收服上**根本不会被加载**，于是 Mixin 的注入校验
 * （{@code require} / {@code Injection validation failed}）也不会跑 —— 门禁绿得毫无意义。
 * 显式 {@code ldc} 一次这些类就能把校验拉回来：锚点写错会在这一刻抛
 * {@code Critical injection failure}，而不是等到某个玩家登录时才炸。
 *
 * <p>类字面量在 mod 侧写的是 mojmap 名，由 loom 按加载器重映射；
 * CraftBukkit 的 {@code org.bukkit.craftbukkit.v.*} 由构建期 {@code versionCraftBukkit} 改写。
 * 探针通过反射调 {@link #loadAll()}（这个类名不参与重映射），三端各跑一次。
 */
public final class PaperarcInjectionCoverage {

    private PaperarcInjectionCoverage() {
    }

    /** NMS 侧：按加载器重映射的类字面量。 */
    private static final Class<?>[] MINECRAFT = {
            net.minecraft.world.entity.projectile.ThrownEgg.class,
            net.minecraft.world.entity.LivingEntity.class,
            net.minecraft.server.level.ServerPlayer.class,
            net.minecraft.server.level.ServerLevel.class,
            net.minecraft.network.Connection.class,
            net.minecraft.server.network.ServerCommonPacketListenerImpl.class,
            net.minecraft.server.network.ServerGamePacketListenerImpl.class,
            net.minecraft.server.players.PlayerList.class,
            net.minecraft.server.commands.KickCommand.class,
            net.minecraft.server.commands.GameModeCommand.class,
            net.minecraft.server.commands.DefaultGameModeCommands.class,
            net.minecraft.world.level.block.NetherPortalBlock.class,
            net.minecraft.world.level.block.EndPortalBlock.class,
    };

    /** CraftBukkit 侧：包名里的 {@code v} 段由构建期任务改成 {@code v1_21_R1}。 */
    private static final Class<?>[] CRAFTBUKKIT = {
            org.bukkit.craftbukkit.v.event.CraftEventFactory.class,
            org.bukkit.craftbukkit.v.CraftWorld.class,
            org.bukkit.craftbukkit.v.CraftOfflinePlayer.class,
            org.bukkit.craftbukkit.v.entity.CraftHumanEntity.class,
            org.bukkit.craftbukkit.v.entity.CraftPlayer.class,
            org.bukkit.craftbukkit.v.entity.CraftLivingEntity.class,
            org.bukkit.craftbukkit.v.potion.CraftPotionUtil.class,
            org.bukkit.craftbukkit.v.util.CraftMagicNumbers.class,
            org.bukkit.craftbukkit.v.enchantments.CraftEnchantment.class,
    };

    /** Bukkit API 侧：事件类与几个具体类（我们给它们加了字段/父接口）。 */
    private static final Class<?>[] BUKKIT = {
            org.bukkit.event.entity.EntityDeathEvent.class,
            org.bukkit.event.entity.PlayerDeathEvent.class,
            org.bukkit.event.entity.EntityPortalEvent.class,
            org.bukkit.event.entity.EntityPortalEnterEvent.class,
            org.bukkit.event.weather.WeatherChangeEvent.class,
            org.bukkit.event.weather.ThunderChangeEvent.class,
            org.bukkit.event.inventory.InventoryCloseEvent.class,
            org.bukkit.event.player.PlayerGameModeChangeEvent.class,
            org.bukkit.event.player.PlayerKickEvent.class,
            org.bukkit.event.player.PlayerQuitEvent.class,
            org.bukkit.event.player.PlayerRespawnEvent.class,
            org.bukkit.event.player.PlayerTeleportEvent.class,
            org.bukkit.event.player.PlayerPortalEvent.class,
            org.bukkit.potion.PotionEffect.class,
            org.bukkit.Sound.class,
            org.bukkit.SoundCategory.class,
            org.bukkit.inventory.ItemStack.class,
    };

    /** Arclight 自己的类：编译期只有 {@code src/arclightStub} 的桩，运行时解析到 Arclight 的实现。 */
    private static final String[] ARCLIGHT = {
            "io.izzel.arclight.common.mod.server.event.ArclightEventFactory",
    };

    /**
     * 拉起全部目标类，返回一行可读的覆盖报告（探针把它打进日志，作为"这一轮注入真的被校验过"的证据）。
     * 有任何一个类加载不出来就抛 —— 那说明清单和代码对不上了。
     */
    public static String loadAll() {
        List<String> failed = new ArrayList<>();
        int loaded = MINECRAFT.length + CRAFTBUKKIT.length + BUKKIT.length;
        for (String name : ARCLIGHT) {
            try {
                Class.forName(name, false, PaperarcInjectionCoverage.class.getClassLoader());
                loaded++;
            } catch (ClassNotFoundException ex) {
                failed.add(name);
            }
        }
        if (!failed.isEmpty()) {
            throw new IllegalStateException("注入覆盖清单里的类加载不出来：" + failed);
        }
        return "nms=" + MINECRAFT.length + " craftbukkit=" + CRAFTBUKKIT.length
                + " bukkit=" + BUKKIT.length + " arclight=" + ARCLIGHT.length
                + " total=" + loaded;
    }
}
