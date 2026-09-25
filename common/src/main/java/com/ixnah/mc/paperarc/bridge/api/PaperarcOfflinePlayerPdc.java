package com.ixnah.mc.paperarc.bridge.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v.persistence.CraftPersistentDataContainer;
import org.bukkit.craftbukkit.v.persistence.CraftPersistentDataTypeRegistry;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;

/**
 * {@code OfflinePlayer#getPersistentDataContainer()} 的真实实现：内容来自
 * {@code <world>/playerdata/<uuid>.dat} 根节点上的 {@code BukkitValues}
 *（就是在线玩家 {@code CraftEntity#storeBukkitValues} 写的那一份），
 * 写入立刻回写同一个文件。
 *
 * <p>B7/Y-4 之前这里返回的是"每次新建的空容器"——读不到已有数据、写了也不落盘。
 *
 * <p>限制（已记 docs/gaps.md）：玩家从没上过线、{@code .dat} 不存在时**不落盘**
 *（凭空造一个只有 BukkitValues 的玩家存档会让 vanilla 读到一个没有位置/背包的玩家）；
 * 玩家在线时由 {@code CraftOfflinePlayerPdcApiMixin} 直接转给在线实体的容器，不走这里。
 *
 * <p>类放在 bridge 里：mixin 里不能出现内部类，也不能用 mixin 自身类型做签名。
 */
public final class PaperarcOfflinePlayerPdc extends CraftPersistentDataContainer {

    /** 与 CraftBukkit 写在实体 NBT 根上的键一致。 */
    private static final String BUKKIT_VALUES = "BukkitValues";

    private final File dataFile;

    /**
     * 直接读 {@code .dat}：1.21.11 的 {@code CraftOfflinePlayer#getData()} 改为返回 {@code ValueInput}，
     * 不再给出根 {@code CompoundTag}，这里自己读同一个文件（写回路径本来就是这么做的）。
     */
    public PaperarcOfflinePlayerPdc(File dataFile, CraftPersistentDataTypeRegistry registry) {
        super(registry);
        this.dataFile = dataFile;
        if (dataFile != null && dataFile.isFile()) {
            try {
                CompoundTag root = NbtIo.readCompressed(dataFile.toPath(), NbtAccounter.unlimitedHeap());
                root.getCompound(BUKKIT_VALUES).ifPresent(this::putAll);
            } catch (Exception ex) {
                throw new IllegalStateException("无法读取离线玩家的 PersistentDataContainer：" + dataFile, ex);
            }
        }
    }

    @Override
    public <T, Z> void set(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        super.set(key, type, value);
        this.save();
    }

    @Override
    public void remove(NamespacedKey key) {
        super.remove(key);
        this.save();
    }

    private void save() {
        if (this.dataFile == null || !this.dataFile.isFile()) {
            return;
        }
        try {
            CompoundTag root = NbtIo.readCompressed(this.dataFile.toPath(), NbtAccounter.unlimitedHeap());
            if (this.isEmpty()) {
                root.remove(BUKKIT_VALUES);
            } else {
                root.put(BUKKIT_VALUES, this.toTagCompound());
            }
            NbtIo.writeCompressed(root, this.dataFile.toPath());
        } catch (Exception ex) {
            throw new IllegalStateException("无法写回离线玩家的 PersistentDataContainer：" + this.dataFile, ex);
        }
    }
}
