package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code InventoryView#title()}（A6/X-2 第五批）。做成抽象基类上的**具体**方法，
 * 一次覆盖 {@code CraftInventoryView} 与插件自定义的 view。
 */
@Mixin(InventoryView.class)
public abstract class InventoryViewApiMixin {

    @Unique
    public Component title() {
        String legacy = ((InventoryView) (Object) this).getTitle();
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }
}
