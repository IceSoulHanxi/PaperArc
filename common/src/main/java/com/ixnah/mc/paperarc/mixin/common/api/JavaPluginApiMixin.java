package com.ixnah.mc.paperarc.mixin.common.api;

import io.papermc.paper.plugin.configuration.PluginMeta;

import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's {@code Plugin#getPluginMeta()} to the runtime {@link JavaPlugin}.
 *
 * <p>Arclight 的 {@code JavaPlugin} 没有让 {@code PluginDescriptionFile} 实现
 * {@code PluginMeta}（Paper 是直接让它实现的），不能强转。原先直接 {@code return null}，
 * 而 paper 的契约是 {@code @NotNull}，插件一用就 NPE（checklist §1.9 af，main 已修）。
 * 改为逐字段适配的 {@code bridge/api/PaperarcPluginMeta}。</p>
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
