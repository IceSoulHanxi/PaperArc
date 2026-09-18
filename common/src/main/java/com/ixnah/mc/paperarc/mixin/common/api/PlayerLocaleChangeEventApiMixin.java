package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Locale;

/**
 * paper 的 {@code PlayerLocaleChangeEvent#locale()}。
 *
 * <p>paper 在构造器里用 {@code Translator.parseLocale(locale)} 把客户端报上来的
 * {@code ll_CC} 串解析成 {@code Locale} 存着；这里按同一规则现算 ——
 * 事件上本来就有 {@code getLocale()}，不需要触发点配合。
 * 解析不出来时返回 {@code Locale.US}，与 adventure 的默认一致。
 */
@Mixin(PlayerLocaleChangeEvent.class)
public abstract class PlayerLocaleChangeEventApiMixin {

    @Unique
    public Locale locale() {
        String raw = ((PlayerLocaleChangeEvent) (Object) this).getLocale();
        Locale parsed = raw == null ? null
                : net.kyori.adventure.translation.Translator.parseLocale(raw);
        return parsed == null ? Locale.US : parsed;
    }
}
