package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.util.CraftIconCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Base64;

/**
 * {@code CachedServerIcon#getData()}（A5-3，pairing 基线 NO_IMPL）。
 *
 * <p>1.20.1 的 {@code CraftIconCache} 只有 {@code public final byte[] value}（javap 核对；
 * 1.19.4 起 favicon 从 base64 串改成了原始 PNG 字节），没有 {@code getData()}。
 * API 约定的"data"是服务器列表里那串 data URI，这里按同一格式编回去。</p>
 */
@Mixin(CraftIconCache.class)
public abstract class CraftIconCacheApiMixin {

    @Shadow
    public byte[] value;

    @Unique
    public String getData() {
        return this.value == null ? null
                : "data:image/png;base64," + Base64.getEncoder().encodeToString(this.value);
    }
}
