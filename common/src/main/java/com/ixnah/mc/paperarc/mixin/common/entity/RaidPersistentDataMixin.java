package com.ixnah.mc.paperarc.mixin.common.entity;

import com.ixnah.mc.paperarc.bridge.RaidPersistentDataBridge;
import com.ixnah.mc.paperarc.bridge.api.PaperarcCommandBlockHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.raid.Raid;
import org.bukkit.craftbukkit.v.persistence.CraftPersistentDataContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Paper 的 {@code Raid extends PersistentDataHolder}：容器挪到 NMS {@code Raid} 字段上，
 * 并随 {@code raids.dat} 落盘（gaps.md §3.1 持久化行）。
 *
 * <p>B7 之前这份容器挂在 {@code ApiState} 里、重启即丢；现在与 CraftBukkit 写实体
 * {@code BukkitValues} 的路子一致：{@code Raid#save(CompoundTag)} 写、
 * 读档构造器 {@code Raid(ServerLevel, CompoundTag)} 读。
 */
@Mixin(Raid.class)
public abstract class RaidPersistentDataMixin implements RaidPersistentDataBridge {

    @Unique
    private static final String PAPERARC_BUKKIT_VALUES = "BukkitValues";

    @Unique
    public CraftPersistentDataContainer persistentDataContainer =
            new CraftPersistentDataContainer(PaperarcCommandBlockHolder.PDC_REGISTRY); // Paper

    @Override
    public CraftPersistentDataContainer paper$persistentDataContainer() {
        return this.persistentDataContainer;
    }

    @Inject(method = "<init>(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/nbt/CompoundTag;)V",
            at = @At("RETURN"))
    private void paperarc$loadPersistentData(ServerLevel level, CompoundTag nbt, CallbackInfo ci) {
        if (nbt.contains(PAPERARC_BUKKIT_VALUES, Tag.TAG_COMPOUND)) {
            this.persistentDataContainer.putAll(nbt.getCompound(PAPERARC_BUKKIT_VALUES));
        }
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void paperarc$savePersistentData(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> cir) {
        if (!this.persistentDataContainer.isEmpty()) {
            nbt.put(PAPERARC_BUKKIT_VALUES, this.persistentDataContainer.toTagCompound());
        }
    }
}
