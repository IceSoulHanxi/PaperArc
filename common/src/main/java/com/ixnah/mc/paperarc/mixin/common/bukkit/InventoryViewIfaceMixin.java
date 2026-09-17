package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.inventory.InventoryView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * B3-3：{@link org.bukkit.inventory.InventoryView} 的 paper default 方法体（照抄 paper-api）。
 */
@Mixin(targets = "org.bukkit.inventory.InventoryView", remap = false)
public interface InventoryViewIfaceMixin {

    @Unique
    public default Component title() {
        InventoryView self = (InventoryView) this;
        return LegacyComponentSerializer.legacySection().deserialize(self.getTitle());
    }
}
