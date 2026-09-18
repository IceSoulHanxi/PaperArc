package com.ixnah.mc.paperarc.bridge;

import com.ixnah.mc.paperarc.bridge.craft.PaperarcLootableData;
import net.kyori.adventure.util.TriState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.bukkit.craftbukkit.v.CraftWorld;
import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.UUID;

/**
 * Y-3 持久化一批的**往返自检**：写 NBT → 清状态 → 读 NBT → 比对。
 *
 * <p>为什么放在 mod 侧而不是探针里：这些断言要直接碰 NMS
 *（{@code Entity#saveWithoutId}/{@code BlockEntity#saveCustomOnly}/{@code Raid#save}），
 * 而探针是插件、Fabric 运行时看到的是 intermediary 名，按 mojmap 名一个都找不到
 *（与 {@link PaperarcInjectionCoverage} 同一条理由）。探针反射调 {@link #roundTrip}，
 * 拿到的是一行可读报告，任何一项对不上就抛。
 *
 * <p>不用真的重启服务器：NBT 往返本身就是存档格式的全部，重启只是多走一遍同样的两个方法。
 */
public final class PaperarcPersistenceSelfTest {

    private PaperarcPersistenceSelfTest() {
    }

    public static String roundTrip(org.bukkit.entity.LivingEntity probe, org.bukkit.block.Block container,
                                   org.bukkit.block.Block beacon) {
        StringBuilder sb = new StringBuilder();
        sb.append(entityRoundTrip(probe));
        sb.append(' ').append(blockEntityRoundTrip(container));
        sb.append(' ').append(beaconRoundTrip(beacon));
        sb.append(' ').append(raidRoundTrip(probe.getWorld()));
        return sb.toString();
    }

    /** 实体侧：origin / spawnReason / fromMobSpawner / frictionState 四样一起过。 */
    private static String entityRoundTrip(org.bukkit.entity.LivingEntity probe) {
        net.minecraft.world.entity.Entity handle = ((CraftEntity) probe).getHandle();
        EntityBridge bridge = (EntityBridge) handle;
        LivingEntityFieldsBridge living = (LivingEntityFieldsBridge) handle;

        org.bukkit.Location origin = probe.getLocation().clone().add(1.5, 2.5, 3.5);
        bridge.setOrigin(origin);
        bridge.paper$setSpawnReason(CreatureSpawnEvent.SpawnReason.SPAWNER);
        bridge.paper$setSpawnedViaMobSpawner(true);
        living.paper$setFrictionState(TriState.FALSE);

        CompoundTag tag = handle.saveWithoutId(new CompoundTag());

        bridge.paper$setSpawnReason(CreatureSpawnEvent.SpawnReason.DEFAULT);
        bridge.paper$setSpawnedViaMobSpawner(false);
        living.paper$setFrictionState(TriState.NOT_SET);
        handle.load(tag);

        require(bridge.paper$spawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER,
                "spawnReason 往返丢了：" + bridge.paper$spawnReason());
        require(bridge.paper$spawnedViaMobSpawner(), "spawnedViaMobSpawner 往返丢了");
        require(living.paper$getFrictionState() == TriState.FALSE,
                "frictionState 往返丢了：" + living.paper$getFrictionState());
        org.bukkit.util.Vector back = bridge.getOriginVector();
        require(back != null && Math.abs(back.getY() - origin.getY()) < 1.0E-6,
                "origin 往返丢了：" + back);
        return "entity=origin+spawnReason+fromSpawner+friction";
    }

    /** 方块实体侧：lootable 记账。 */
    private static String blockEntityRoundTrip(org.bukkit.block.Block container) {
        ServerLevel level = ((CraftWorld) container.getWorld()).getHandle();
        BlockEntity blockEntity = level.getBlockEntity(
                new BlockPos(container.getX(), container.getY(), container.getZ()));
        require(blockEntity != null, "拿不到容器方块实体：" + container.getType());

        UUID looter = UUID.randomUUID();
        PaperarcLootableData.of(blockEntity).recordFill(looter);
        long filled = PaperarcLootableData.of(blockEntity).getLastFilled();
        require(filled != -1L, "recordFill 之后 lastFilled 仍是 -1");

        HolderLookup.Provider registries = level.registryAccess();
        CompoundTag tag = blockEntity.saveCustomOnly(registries);
        require(tag.contains("Paper.LootableData"), "lootable 记账没写进方块实体 NBT");

        PaperarcLootableData.of(blockEntity).setHasPlayerLooted(looter, false);
        PaperarcLootableData.of(blockEntity).setNextRefill(-1L);
        blockEntity.loadCustomOnly(tag, registries);

        PaperarcLootableData back = PaperarcLootableData.of(blockEntity);
        require(back.getLastFilled() == filled, "lastFilled 往返对不上");
        require(back.hasPlayerLooted(looter), "lootedPlayers 往返丢了");
        return "blockEntity=lootable";
    }

    /** 信标：effectRange 落盘 + 读回。 */
    private static String beaconRoundTrip(org.bukkit.block.Block beacon) {
        ServerLevel level = ((CraftWorld) beacon.getWorld()).getHandle();
        BlockEntity blockEntity = level.getBlockEntity(
                new BlockPos(beacon.getX(), beacon.getY(), beacon.getZ()));
        require(blockEntity instanceof BeaconBlockEntityBridge, "拿不到信标方块实体");

        BeaconBlockEntityBridge bridge = (BeaconBlockEntityBridge) blockEntity;
        bridge.paper$setEffectRange(42.5);
        HolderLookup.Provider registries = level.registryAccess();
        CompoundTag tag = blockEntity.saveCustomOnly(registries);
        require(tag.contains("Paper.Range"), "effectRange 没写进信标 NBT");

        bridge.paper$setEffectRange(-1);
        blockEntity.loadCustomOnly(tag, registries);
        require(Math.abs(bridge.paper$getEffectRange() - 42.5) < 1.0E-6,
                "effectRange 往返对不上：" + bridge.paper$getEffectRange());
        return "beacon=effectRange";
    }

    /** 袭击：PDC 随 raids.dat 落盘。构造一个不注册进管理器的 Raid 就够验格式。 */
    private static String raidRoundTrip(org.bukkit.World world) {
        ServerLevel level = ((CraftWorld) world).getHandle();
        Raid raid = new Raid(1, level, new BlockPos(0, 64, 0));
        RaidPersistentDataBridge bridge = (RaidPersistentDataBridge) raid;
        bridge.paper$persistentDataContainer().set(
                new org.bukkit.NamespacedKey("paperarc", "selftest"),
                org.bukkit.persistence.PersistentDataType.STRING, "y3");

        CompoundTag tag = raid.save(new CompoundTag());
        require(tag.contains("BukkitValues"), "Raid 的 PDC 没写进 NBT");

        Raid reloaded = new Raid(level, tag);
        String back = ((RaidPersistentDataBridge) reloaded).paper$persistentDataContainer().get(
                new org.bukkit.NamespacedKey("paperarc", "selftest"),
                org.bukkit.persistence.PersistentDataType.STRING);
        require("y3".equals(back), "Raid 的 PDC 往返对不上：" + back);
        return "raid=pdc";
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException("持久化往返自检失败：" + message);
        }
    }
}
