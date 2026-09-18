package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.inventory.InventoryType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** paper 的 {@code InventoryType#defaultTitle()}（A6/X-2 第六批，包装已有的 getDefaultTitle）。 */
@Mixin(InventoryType.class)
public abstract class InventoryTypeApiMixin {

    @Unique
    @SuppressWarnings("deprecation")
    public Component defaultTitle() {
        String legacy = ((InventoryType) (Object) this).getDefaultTitle();
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }
}
