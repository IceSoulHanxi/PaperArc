package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code RegisteredListener#getExecutor()}（A6/X-2 第五批）。
 *
 * <p>paper 还重写了 {@code toString()}，但 {@code @Unique} 方法撞上目标已有的同签名方法
 * 会被 Mixin 整个丢弃（只留一行 WARN，等于没加，见状态文档第五章），所以 toString
 * 不补，记在 docs/gaps.md。
 */
@Mixin(RegisteredListener.class)
public abstract class RegisteredListenerApiMixin {

    @Shadow
    private EventExecutor executor;

    @Unique
    public EventExecutor getExecutor() {
        return this.executor;
    }
}
