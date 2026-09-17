package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.plugin.Plugin} (generated).
 * Adds 1 paper-api method declaration(s); implementations live in
 * the Craft* @Unique mixins (com.ixnah.mc.paperarc.mixin.common.api).*
 */
@Mixin(targets = "org.bukkit.plugin.Plugin", remap = false)
public interface PluginIfaceMixin {

    @Unique
    public abstract io.papermc.paper.plugin.configuration.PluginMeta getPluginMeta();

    /**
     * paper-api 在 {@code Plugin} 上以 default 方法提供三个 logger 入口，运行时
     * Arclight 的 {@code Plugin} 接口完全没有它们（插件直接调 default 方法同样
     * {@code NoSuchMethodError}，见 checklist §1.2(e)）。interface mixin 的 default
     * 方法体会随接口一起合并进目标，方法体照抄 paper-api 的 default 实现。
     *
     * <p>{@code this} 强转成 {@code Plugin} 而不是 {@code @Shadow} —— 目标就是
     * {@code Plugin} 本身，接口间强转编译期恒成立。
     */
    @Unique
    public default net.kyori.adventure.text.logger.slf4j.ComponentLogger getComponentLogger() {
        return net.kyori.adventure.text.logger.slf4j.ComponentLogger.logger(
                ((org.bukkit.plugin.Plugin) this).getName());
    }

    @Unique
    public default org.slf4j.Logger getSLF4JLogger() {
        return org.slf4j.LoggerFactory.getLogger(((org.bukkit.plugin.Plugin) this).getLogger().getName());
    }

    @Unique
    public default org.apache.logging.log4j.Logger getLog4JLogger() {
        return org.apache.logging.log4j.LogManager.getLogger(
                ((org.bukkit.plugin.Plugin) this).getLogger().getName());
    }

}