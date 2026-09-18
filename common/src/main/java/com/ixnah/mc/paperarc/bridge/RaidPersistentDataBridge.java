package com.ixnah.mc.paperarc.bridge;

/**
 * Paper 给 NMS {@code Raid} 加的 {@code persistentDataContainer} 字段
 * （{@code Raid extends PersistentDataHolder}）。
 *
 * <p>B8/Y-3 从 {@code ApiState} 挪到 NMS 字段并随 {@code raids.dat} 落盘
 * （键 {@code BukkitValues}，与实体/方块实体侧一致）。
 */
public interface RaidPersistentDataBridge {

    org.bukkit.craftbukkit.v.persistence.CraftPersistentDataContainer paper$persistentDataContainer();
}
