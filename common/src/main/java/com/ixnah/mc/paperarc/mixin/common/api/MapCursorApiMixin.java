package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.map.MapCursor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code MapCursor} Component 版 caption（A6/X-2 第五批）。
 * paper 内部把 caption 存成 Component、String 版反过来序列化；运行时存的是 String，
 * 这里方向相反但往返一致。
 */
@Mixin(MapCursor.class)
public abstract class MapCursorApiMixin {

    @Unique
    public Component caption() {
        String legacy = ((MapCursor) (Object) this).getCaption();
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public void caption(Component caption) {
        ((MapCursor) (Object) this).setCaption(caption == null ? null
                : LegacyComponentSerializer.legacySection().serialize(caption));
    }
}
