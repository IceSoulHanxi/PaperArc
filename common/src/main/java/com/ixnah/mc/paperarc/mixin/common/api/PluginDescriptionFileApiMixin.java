package com.ixnah.mc.paperarc.mixin.common.api;

import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoadOrder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Collections;
import java.util.List;

/**
 * 让 {@code PluginDescriptionFile} 实现 paper 的 {@code PluginMeta} 并补它的 7 个方法
 * （A6/X-2 第五批）。全是对已有 getter 的重命名转发，没有新状态。
 *
 * <p>这样 {@code Plugin#getPluginMeta()} 就能直接返回 description file 本身；A5-2 af
 * 做的 {@code bridge/api/PaperarcPluginMeta} 适配器仍然保留（它负责 Plugin 侧的那条路，
 * 且对第三方自定义 PluginDescriptionFile 子类更稳）。
 */
@Mixin(PluginDescriptionFile.class)
public abstract class PluginDescriptionFileApiMixin implements PluginMeta {

    @Unique
    private PluginDescriptionFile paperarc$self() {
        return (PluginDescriptionFile) (Object) this;
    }

    @Unique
    public String getMainClass() {
        return this.paperarc$self().getMain();
    }

    @Unique
    public PluginLoadOrder getLoadOrder() {
        return this.paperarc$self().getLoad();
    }

    @Unique
    public String getLoggerPrefix() {
        return this.paperarc$self().getPrefix();
    }

    @Unique
    public List<String> getPluginDependencies() {
        List<String> depend = this.paperarc$self().getDepend();
        return depend == null ? Collections.emptyList() : depend;
    }

    @Unique
    public List<String> getPluginSoftDependencies() {
        List<String> soft = this.paperarc$self().getSoftDepend();
        return soft == null ? Collections.emptyList() : soft;
    }

    @Unique
    public List<String> getLoadBeforePlugins() {
        List<String> before = this.paperarc$self().getLoadBefore();
        return before == null ? Collections.emptyList() : before;
    }

    @Unique
    public List<String> getProvidedPlugins() {
        List<String> provides = this.paperarc$self().getProvides();
        return provides == null ? Collections.emptyList() : provides;
    }
}
