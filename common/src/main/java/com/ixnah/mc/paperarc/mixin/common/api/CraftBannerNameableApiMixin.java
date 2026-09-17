package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import org.bukkit.craftbukkit.v.block.CraftBanner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code Banner extends org.bukkit.Nameable}（B3-2）。
 *
 * <p>{@code customName()}/{@code customName(Component)} 已由
 * {@code CraftBlockEntityState} 上的实现体覆盖（所有方块状态共用）；只有 String 版的
 * {@code getCustomName}/{@code setCustomName} 是 spigot 侧按类逐个实现的，横幅没有，
 * 所以补在这里。NMS {@code BannerBlockEntity.name} 是 private final 之外的普通 private 字段，
 * 已在 paperarc.accesswidener 放开。
 */
@Mixin(CraftBanner.class)
public abstract class CraftBannerNameableApiMixin {

    @Unique
    private BannerBlockEntity paperarc$banner() {
        return (BannerBlockEntity) ((CraftBlockEntityStateBridge) (Object) this).paperarc$getSnapshot();
    }

    @Unique
    public String getCustomName() {
        net.minecraft.network.chat.Component name = paperarc$banner().getCustomName();
        return name == null ? null : LegacyComponentSerializer.legacySection()
                .serialize(net.kyori.adventure.text.Component.text(name.getString()));
    }

    @Unique
    public void setCustomName(String name) {
        paperarc$banner().name = name == null ? null
                : net.minecraft.network.chat.Component.literal(name);
    }
}
