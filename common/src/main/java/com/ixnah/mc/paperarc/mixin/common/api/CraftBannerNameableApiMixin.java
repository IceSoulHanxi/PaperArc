package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import org.bukkit.craftbukkit.v.block.CraftBanner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code Banner extends org.bukkit.Nameable}（A5-1）的 String 侧实现体。
 *
 * <p>{@code customName()}/{@code customName(Component)} 已由所有方块状态共用的
 * {@code CraftBlockEntityStateNameableApiMixin} 覆盖；只有 String 版的
 * {@code getCustomName}/{@code setCustomName} 是 spigot 按类逐个实现的，横幅没有。
 * 1.20.1 的 {@code BannerBlockEntity} 自带 public 的 {@code getCustomName}/
 * {@code setCustomName}（javap 核对），不需要 AT。</p>
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
        paperarc$banner().setCustomName(name == null ? null
                : net.minecraft.network.chat.Component.literal(name));
    }
}
