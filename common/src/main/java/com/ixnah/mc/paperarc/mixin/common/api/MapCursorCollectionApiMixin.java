package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursorCollection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** paper 的 {@code MapCursorCollection#addCursor(…, Component)}（A6/X-2 第五批）。 */
@Mixin(MapCursorCollection.class)
public abstract class MapCursorCollectionApiMixin {

    @Unique
    @SuppressWarnings("deprecation")
    public MapCursor addCursor(int x, int y, byte direction, byte type, boolean visible,
                               Component caption) {
        return ((MapCursorCollection) (Object) this).addCursor(x, y, direction, type, visible,
                caption == null ? null : LegacyComponentSerializer.legacySection().serialize(caption));
    }
}
