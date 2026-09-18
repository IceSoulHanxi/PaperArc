package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.configuration.file.YamlConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.io.File;

/**
 * paper 的 {@code Server.Spigot#getBukkitConfig()/getSpigotConfig()/getPaperConfig()}
 * （A6/X-2 第五批）。
 *
 * <p>paper-api 的基类版本是直接抛 {@code UnsupportedOperationException}，真正的实现在
 * {@code CraftServer.Spigot}（运行时没有）。这里**读真实配置文件**而不是抛异常：
 * bukkit.yml / spigot.yml 就在工作目录下。Arclight 没有 paper.yml，
 * {@code getPaperConfig()} 返回空配置（语义差异记在 docs/gaps.md）。
 */
@Mixin(org.bukkit.Server.Spigot.class)
public abstract class ServerSpigotApiMixin {

    @Unique
    private static YamlConfiguration paperarc$load(String fileName) {
        File file = new File(fileName);
        return file.isFile() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
    }

    @Unique
    public YamlConfiguration getBukkitConfig() {
        return paperarc$load("bukkit.yml");
    }

    @Unique
    public YamlConfiguration getSpigotConfig() {
        return paperarc$load("spigot.yml");
    }

    @Unique
    public YamlConfiguration getPaperConfig() {
        // Arclight 不带 paper 配置；返回空配置而不是抛异常，插件读到的就是"全默认"
        return paperarc$load("paper.yml");
    }
}
