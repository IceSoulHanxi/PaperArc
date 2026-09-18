package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.NamespacedKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 加在 {@code NamespacedKey} 上的 3 个方法（checklist §1.10 am，第 ⑤ 批）：
 * adventure {@code Key} 的 {@code namespace()/value()/asString()}。
 * paper 读私有字段，这里换公开 getter，取值一致。
 */
@Mixin(NamespacedKey.class)
public abstract class NamespacedKeyApiMixin {

    @Unique
    private NamespacedKey paperarc$self() {
        return (NamespacedKey) (Object) this;
    }

    @Unique
    public String namespace() {
        return this.paperarc$self().getNamespace();
    }

    @Unique
    public String value() {
        return this.paperarc$self().getKey();
    }

    @Unique
    public String asString() {
        NamespacedKey self = this.paperarc$self();
        return self.getNamespace() + ":" + self.getKey();
    }
}
