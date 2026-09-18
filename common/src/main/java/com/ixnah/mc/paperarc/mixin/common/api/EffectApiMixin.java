package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.Effect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code Effect#isApplicable(Object)}（A6/X-2 第三批）。
 *
 * <p>paper 把 {@code Effect.data} 改成了 {@code List<Class<?>>} 并逐个判；运行时（spigot）
 * 那个字段仍是单个 {@code Class<?>}，公开的 {@code getData()} 正好返回它，语义等价。
 */
@Mixin(Effect.class)
public abstract class EffectApiMixin {

    @Unique
    @SuppressWarnings("deprecation")
    public boolean isApplicable(Object obj) {
        Class<?> data = ((Effect) (Object) this).getData();
        return data != null && obj != null && data.isAssignableFrom(obj.getClass());
    }
}
