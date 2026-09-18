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
            // B8 批 3b：触发点上下文
            net.minecraft.server.level.ServerPlayerGameMode.class,
            net.minecraft.world.entity.ExperienceOrb.class,
            net.minecraft.world.entity.Entity.class,
            net.minecraft.world.entity.player.Player.class,
            net.minecraft.world.entity.projectile.AbstractArrow.class,
            net.minecraft.world.food.FoodData.class,
            net.minecraft.world.item.BlockItem.class,
            net.minecraft.world.item.StandingAndWallBlockItem.class,
            net.minecraft.world.level.block.entity.CampfireBlockEntity.class,
            net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.class,
            net.minecraft.server.PlayerAdvancements.class,
            // B8 批 3c：事件字段 + 触发点消费
            net.minecraft.world.entity.item.ItemEntity.class,
            net.minecraft.world.entity.Leashable.class,
            net.minecraft.network.protocol.game.ClientboundOpenScreenPacket.class,
            // B8 批 3d：登录路径（只在玩家登录时才加载，不显式拉起就验不到锚点）
            net.minecraft.server.network.ServerLoginPacketListenerImpl.class,
            // B8/Y-3：持久化一批的注入宿主
            net.minecraft.world.level.block.entity.BlockEntity.class,
            net.minecraft.world.level.block.entity.BeaconBlockEntity.class,
            net.minecraft.world.entity.raid.Raid.class,
            net.minecraft.world.entity.boss.enderdragon.EnderDragon.class,
            net.minecraft.world.entity.monster.Evoker.class,
            // B8/Y-4
            net.minecraft.world.entity.npc.AbstractVillager.class,
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
            // B8/Y-4
            org.bukkit.craftbukkit.v.inventory.CraftItemStack.class,
            org.bukkit.craftbukkit.v.generator.CraftWorldInfo.class,
            org.bukkit.craftbukkit.v.entity.CraftVillager.class,
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
            // B8 批 3a：由事件自身状态即可得出的一组
            org.bukkit.event.server.TabCompleteEvent.class,
            org.bukkit.event.player.PlayerItemDamageEvent.class,
            org.bukkit.event.vehicle.VehicleExitEvent.class,
            org.bukkit.event.entity.EntityDismountEvent.class,
            org.bukkit.event.player.PlayerResourcePackStatusEvent.class,
            org.bukkit.event.block.BlockPhysicsEvent.class,
            org.bukkit.entity.Player.Spigot.class,
            // B8 批 3b
            org.bukkit.event.player.PlayerInteractEvent.class,
            org.bukkit.event.block.BlockDamageEvent.class,
            org.bukkit.event.player.PlayerExpChangeEvent.class,
            org.bukkit.event.block.BlockCanBuildEvent.class,
            org.bukkit.event.block.BlockCookEvent.class,
            org.bukkit.event.entity.EntityRegainHealthEvent.class,
            org.bukkit.event.vehicle.VehicleBlockCollisionEvent.class,
            org.bukkit.event.player.PlayerAdvancementDoneEvent.class,
            org.bukkit.event.entity.EntityDamageByEntityEvent.class,
            // B8 批 3c
            org.bukkit.event.inventory.FurnaceBurnEvent.class,
            org.bukkit.event.player.PlayerItemConsumeEvent.class,
            org.bukkit.event.player.PlayerPickupItemEvent.class,
            org.bukkit.event.inventory.InventoryOpenEvent.class,
            org.bukkit.event.entity.EntityUnleashEvent.class,
            org.bukkit.event.entity.EntityShootBowEvent.class,
            org.bukkit.event.player.AsyncPlayerPreLoginEvent.class,
            // B8 批 3e
            org.bukkit.event.block.BrewingStartEvent.class,
            org.bukkit.event.player.PlayerLocaleChangeEvent.class,
            org.bukkit.plugin.RegisteredListener.class,
            // Y-5 补了枚举常量的目标枚举（<clinit> TAIL 注入；不显式加载就验不到）
            org.bukkit.PortalType.class,
            org.bukkit.Fluid.class,
            org.bukkit.entity.Boat.Status.class,
            org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.class,
            org.bukkit.event.entity.EntityRemoveEvent.Cause.class,
            org.bukkit.event.entity.EntityTargetEvent.TargetReason.class,
            org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult.class,
            org.bukkit.event.player.PlayerFishEvent.State.class,
            org.bukkit.MinecraftExperimental.Requires.class,
            org.bukkit.scoreboard.DisplaySlot.class,
            org.bukkit.Effect.class,
    };

    /**
     * 匿名内部类：源码里写不出类字面量，只能按"外层类的运行时名 + $序号"拼。
     * 外层用类字面量取名，所以 CraftBukkit 的 {@code v} 段仍由构建期任务改写，这里不写死版本。
     */
    private static final String[] NESTED = {
            org.bukkit.craftbukkit.v.entity.CraftPlayer.class.getName() + "$2",
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
        for (String name : NESTED) {
            try {
                Class.forName(name, false, PaperarcInjectionCoverage.class.getClassLoader());
                loaded++;
            } catch (ClassNotFoundException ex) {
                failed.add(name);
            }
        }
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
                + " bukkit=" + BUKKIT.length + " nested=" + NESTED.length
                + " arclight=" + ARCLIGHT.length + " total=" + loaded;
    }
}
