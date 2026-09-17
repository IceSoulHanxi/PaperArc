package com.ixnah.mc.paperarc.mixin.common.api;

import io.papermc.paper.plugin.configuration.PluginMeta;

import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's {@code Plugin#getPluginMeta()} to the runtime {@link JavaPlugin}.
 *
 * <p>Arclight 的 {@code PluginDescriptionFile} 没有实现 {@code PluginMeta}（paper 是
 * 直接让它实现的），不能强转；改为用 {@code bridge.api.PaperarcPluginMeta} 逐字段适配。
 * 原先这里 {@code return null}，插件按 paper 的 {@code @NotNull} 契约用就是 NPE
 * （B3-3 探针 P19 实测）。</p>
 */
@Mixin(JavaPlugin.class)
public abstract class JavaPluginApiMixin {

    @Shadow
    public abstract org.bukkit.plugin.PluginDescriptionFile getDescription();

    @Unique
    public PluginMeta getPluginMeta() {
        return new com.ixnah.mc.paperarc.bridge.api.PaperarcPluginMeta(this.getDescription());
    }

    /**
     * paper {@code Plugin#getLifecycleManager()}（B3-4）。
     *
     * <p><b>占位实现</b>：抛 {@code UnsupportedOperationException}。这套 API 要求 paper 的
     * 插件引导（{@code PluginBootstrap}）与 Brigadier 命令注册管线，Arclight 的插件加载器
     * 两样都没有；返回一个空管理器会让插件以为注册成功、回调却永不触发，比直接报错更坏。
     * 语义差异记 `docs/gaps.md`。
     */
    @Unique
    public io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager<org.bukkit.plugin.Plugin>
            getLifecycleManager() {
        throw new UnsupportedOperationException(
                "PaperArc: Arclight 没有 paper 的插件生命周期/Brigadier 管线，"
                        + "Plugin#getLifecycleManager() 不可用（docs/gaps.md）");
    }
}
