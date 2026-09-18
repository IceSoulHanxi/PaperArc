package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Locale;

/**
 * paper 的 {@code PlayerLocaleChangeEvent#locale()}（A6/X-2 第六批）：把事件已有的
 * {@code getLocale()} 字符串（客户端发的 {@code zh_cn} 这种）解析成 {@code Locale}。
 * 解析规则与 paper 一致：下划线换连字符后走 {@code Locale.forLanguageTag}。
 */
@Mixin(PlayerLocaleChangeEvent.class)
public abstract class PlayerLocaleChangeEventApiMixin {

    @Unique
    public Locale locale() {
        String raw = ((PlayerLocaleChangeEvent) (Object) this).getLocale();
        if (raw == null) {
            return null;
        }
        Locale parsed = Locale.forLanguageTag(raw.replace('_', '-'));
        return parsed.getLanguage().isEmpty() ? null : parsed;
    }
}
