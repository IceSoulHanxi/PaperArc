package com.ixnah.mc.paperarc.mixin.common.bukkit;

import org.bukkit.plugin.Plugin;
import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.logging.Logger;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Server;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Interface augmentation for {@link org.bukkit.plugin.Plugin}.
 *
 * <p>paper-api 在 {@code Plugin} 上以 default 方法提供三个 logger 入口，运行时
 * Arclight 的 {@code Plugin} 接口完全没有它们 —— 插件直接调 default 方法同样
 * {@code NoSuchMethodError}（checklist §1.2(e) 的 228 条 default 缺口之一）。
 * interface mixin 的 default 方法体会随接口一起合并进目标（Phase A2-1 在 1.20.1 实测），
 * 所以直接照抄 paper-api 的 default 实现即可，不必在每个 JavaPlugin 上落地。
 *
 * <p>{@code this} 强转成 {@code Plugin} 而不是 {@code @Shadow}：目标就是
 * {@code Plugin} 本身，接口间强转编译期恒成立，比 @Shadow 稳。
 */
@Mixin(targets = "org.bukkit.plugin.Plugin", remap = false)
public interface PluginIfaceMixin extends io.papermc.paper.plugin.lifecycle.event.LifecycleEventOwner {

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

    @Unique
    public abstract io.papermc.paper.plugin.configuration.PluginMeta getPluginMeta();

    @Unique
    public default Path getDataPath() {
        Plugin self = (Plugin) this;
        return self.getDataFolder().toPath();
    }

    /** B3-4：实现体在 api/JavaPluginApiMixin（占位，抛 UnsupportedOperationException）。 */
    @Unique
    public abstract io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager<org.bukkit.plugin.Plugin>
            getLifecycleManager();
}
