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
}
