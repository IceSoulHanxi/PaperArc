package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import org.bukkit.craftbukkit.v.block.CraftBlockEntityState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper 的 {@code Nameable#customName()} / {@code customName(Component)} 在方块侧的实现体。
 *
 * <p>{@code Nameable} 有两族互不继承的实现类：实体（{@code CraftEntity}，见
 * {@code CraftEntityApiMixin}）与方块状态。方块侧此前完全没有实现体，
 * {@code CraftChest}/{@code CraftBeacon}/… 12 个类走到就是 {@code AbstractMethodError}
 * （A4-1 r）。宿主取它们的公共基类 {@code CraftBlockEntityState}，一次覆盖全部。
 *
 * <p>与其它方块状态 API 一致：读写的是快照（snapshot），插件需要 {@code update()} 落盘。
 * Component ↔ NMS 的转换沿用本仓库的 gson 往返（PaperAdventure 不可用）。
 */
@Mixin(CraftBlockEntityState.class)
public abstract class CraftBlockEntityStateNameableApiMixin {

    @Unique
    private BlockEntity paperarc$nameableSnapshot() {
        return ((CraftBlockEntityStateBridge) (Object) this).paperarc$getSnapshot();
    }

    @Unique
    public net.kyori.adventure.text.Component customName() {
        BlockEntity snapshot = this.paperarc$nameableSnapshot();
        net.minecraft.network.chat.Component nms = snapshot instanceof net.minecraft.world.Nameable nameable
                ? nameable.getCustomName() : null;
        return nms == null ? null
                : GsonComponentSerializer.gson().deserialize(
                        net.minecraft.network.chat.Component.Serializer.toJson(nms));
    }

    @Unique
    public void customName(net.kyori.adventure.text.Component customName) {
        net.minecraft.network.chat.Component nms = customName == null ? null
                : net.minecraft.network.chat.Component.Serializer.fromJson(
                        GsonComponentSerializer.gson().serialize(customName));
        BlockEntity snapshot = this.paperarc$nameableSnapshot();
        // setCustomName 不在 NMS Nameable 接口上，逐族转调（javap 核对：这三族覆盖
        // 全部实现 org.bukkit.Nameable 的方块状态）
        if (snapshot instanceof BaseContainerBlockEntity container) {
            container.setCustomName(nms);
        } else if (snapshot instanceof BeaconBlockEntity beacon) {
            beacon.setCustomName(nms);
        } else if (snapshot instanceof EnchantmentTableBlockEntity table) {
            table.setCustomName(nms);
        }
    }
}
