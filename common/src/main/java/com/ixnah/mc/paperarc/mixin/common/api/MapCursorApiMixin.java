package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import org.bukkit.map.MapCursor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 加在 {@code MapCursor} 上的 {@code caption()} 一对（checklist §1.10 am，第 ⑤ 批）。
 *
 * <p>状态字段直接挂在 MapCursor 上：它是插件自己 new 出来、按值使用的普通对象，
 * 不存在"包装对象身份"问题（X-1 的口径）。</p>
 */
@Mixin(MapCursor.class)
public abstract class MapCursorApiMixin {

    @Unique
    private Component paperarc$caption;

    @Unique
    public Component caption() {
        return this.paperarc$caption;
    }

    @Unique
    public void caption(Component caption) {
        this.paperarc$caption = caption;
    }
}
