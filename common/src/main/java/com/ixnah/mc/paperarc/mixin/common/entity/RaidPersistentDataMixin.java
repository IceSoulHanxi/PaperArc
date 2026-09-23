package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.RaidPersistentDataBridge;
import com.ixnah.mc.paperarc.bridge.api.PaperarcCommandBlockHolder;
import net.minecraft.world.entity.raid.Raid;
import org.bukkit.craftbukkit.v.persistence.CraftPersistentDataContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper 的 {@code Raid extends PersistentDataHolder}：容器挂在 NMS {@code Raid} 字段上。
 *
 * <p>TODO(1.21.11): 1.21.1 时随 {@code raids.dat} 以 {@code BukkitValues} 键落盘（挂
 * {@code Raid#save(CompoundTag)} 与读档构造器）；1.21.11 的 Raid 改由 {@code MAP_CODEC}
 * 编解码，这两个锚点都没了，目前容器只在内存里、重启即丢。按 C3 原则改挂 codec，键名保持
 * {@code BukkitValues}。
 */
@Mixin(Raid.class)
public abstract class RaidPersistentDataMixin implements RaidPersistentDataBridge {

    @Unique
    public CraftPersistentDataContainer persistentDataContainer =
            new CraftPersistentDataContainer(PaperarcCommandBlockHolder.PDC_REGISTRY);

    @Override
    public CraftPersistentDataContainer paper$persistentDataContainer() {
        return this.persistentDataContainer;
    }
}
