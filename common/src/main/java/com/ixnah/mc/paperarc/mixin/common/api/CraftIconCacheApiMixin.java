package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.craftbukkit.v.util.CraftIconCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Paper 的 {@code CachedServerIcon#getData()}：返回服务器列表 ping 用的
 * {@code data:image/png;base64,...} 串。Arclight 的 CraftIconCache 存的是原始字节，
 * 这里按 vanilla ping 包的格式拼出来。
 */
@Mixin(CraftIconCache.class)
public abstract class CraftIconCacheApiMixin {

    @Shadow
    public byte[] value;

    @Unique
    public String getData() {
        return this.value == null ? null
                : "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(this.value);
    }
}
