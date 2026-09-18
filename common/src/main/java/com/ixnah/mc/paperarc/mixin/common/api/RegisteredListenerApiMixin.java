package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code RegisteredListener#getExecutor()} 与 {@code toString()}。
 *
 * <p>{@code executor} 字段运行时就有（只是没有 getter）；{@code toString()} 照抄
 * paper 的拼法。{@code toString()} 同 {@link DisplaySlotApiMixin} 那条：
 * 不写 {@code @Unique}，否则会因为与继承自 {@code Object} 的同签名方法撞名被丢弃。
 */
@Mixin(RegisteredListener.class)
public abstract class RegisteredListenerApiMixin {

    @Shadow(remap = false)
    @Final
    private EventExecutor executor;

    @Unique
    public EventExecutor getExecutor() {
        return this.executor;
    }

    @Override
    public String toString() {
        RegisteredListener self = (RegisteredListener) (Object) this;
        return "RegisteredListener{plugin=" + self.getPlugin().getName()
                + ", listener=" + self.getListener()
                + ", executor=" + this.executor
                + ", priority=" + self.getPriority()
                + ", ignoreCancelled=" + self.isIgnoringCancelled() + '}';
    }
}
