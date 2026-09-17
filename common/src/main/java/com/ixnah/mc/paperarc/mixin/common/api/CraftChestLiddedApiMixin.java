package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import org.bukkit.craftbukkit.v.block.CraftChest;
import org.bukkit.craftbukkit.v.block.CraftEnderChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code Lidded#isOpen()} 的箱子实现体（PARTIAL_IMPL 门禁抓到：Barrel/ShulkerBox
 * 有实现，Chest/EnderChest 没有）。
 *
 * <p>vanilla 的 {@code openersCounter} 是 private 且没有访问器，由
 * {@code paperarc.accesswidener} 放开后直接读开启人数（与 Barrel 的做法一致）。</p>
 */
@Mixin({CraftChest.class, CraftEnderChest.class})
public abstract class CraftChestLiddedApiMixin {

    @Unique
    public boolean isOpen() {
        Object snapshot = ((CraftBlockEntityStateBridge) (Object) this).paperarc$getSnapshot();
        if (snapshot instanceof ChestBlockEntity chest) {
            return chest.openersCounter.getOpenerCount() > 0;
        }
        if (snapshot instanceof EnderChestBlockEntity enderChest) {
            return enderChest.openersCounter.getOpenerCount() > 0;
        }
        return false;
    }
}
