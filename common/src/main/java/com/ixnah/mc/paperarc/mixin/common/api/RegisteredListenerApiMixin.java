package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code RegisteredListener#getExecutor()} 与 {@code toString()}。
 *
 * <p>A8/Y-4（checklist bo）把 {@code toString()} 补上了：运行时的
 * {@code RegisteredListener} **并没有**自己声明 {@code toString()}
 * （`javap -p` 核对，只继承 {@code Object} 的），所以既不能 {@code @Overwrite}
 * （目标里找不到该方法），也不能带 {@code @Unique}
 * （Mixin 把继承来的同签名方法也算冲突，整条丢弃）。
 * 写成**不带 {@code @Unique} 的普通方法**：Mixin 直接合并进目标类，
 * 语义上就是覆盖 {@code Object#toString()}。文本与 paper 逐字一致，便于对拍日志。
 */
@Mixin(RegisteredListener.class)
public abstract class RegisteredListenerApiMixin {

    @Shadow
    private EventExecutor executor;

    @Shadow
    @Final
    private Listener listener;

    @Shadow
    @Final
    private EventPriority priority;

    @Shadow
    @Final
    private Plugin plugin;

    @Shadow
    @Final
    private boolean ignoreCancelled;

    @Unique
    public EventExecutor getExecutor() {
        return this.executor;
    }

    @Override
    public String toString() {
        return "RegisteredListener{plugin=" + this.plugin.getName()
                + ", listener=" + this.listener
                + ", executor=" + this.executor
                + ", priority=" + this.priority.name() + "(" + this.priority.getSlot() + ")"
                + ", ignoreCancelled=" + this.ignoreCancelled + "}";
    }
}
