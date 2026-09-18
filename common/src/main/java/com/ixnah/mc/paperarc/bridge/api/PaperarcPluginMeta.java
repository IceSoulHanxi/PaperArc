package com.ixnah.mc.paperarc.bridge.api;

import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoadOrder;

import java.util.List;

/**
 * 把 Arclight 的 {@link PluginDescriptionFile} 适配成 paper 的 {@link PluginMeta}
 * （{@code Plugin#getPluginMeta()}）。
 *
 * <p>Paper 直接让 {@code PluginDescriptionFile} 实现 {@code PluginMeta}，Arclight 没有；
 * 原先的实现体直接 {@code return null}，插件按 paper 的 {@code @NotNull} 契约用就是
 * {@code NullPointerException}（checklist §1.9 af）。这里逐字段转发，两边语义一一对应。
 *
 * <p>不是 mixin 的内部类：mixin 里的内嵌/匿名类合并后 InnerClasses 会与目标矛盾
 * （docs/mixin-conventions.md）。
 */
public final class PaperarcPluginMeta implements PluginMeta {

    private final PluginDescriptionFile description;

    public PaperarcPluginMeta(PluginDescriptionFile description) {
        this.description = description;
    }

    @Override
    public String getName() {
        return this.description.getName();
    }

    @Override
    public String getMainClass() {
        return this.description.getMain();
    }

    @Override
    public PluginLoadOrder getLoadOrder() {
        return this.description.getLoad();
    }

    @Override
    public String getVersion() {
        return this.description.getVersion();
    }

    /** paper 的 logger 前缀没配时用插件名，与 {@code PluginDescriptionFile#getPrefix} 的空值语义一致。 */
    @Override
    public String getLoggerPrefix() {
        String prefix = this.description.getPrefix();
        return prefix == null ? this.description.getName() : prefix;
    }

    @Override
    public List<String> getPluginDependencies() {
        return this.description.getDepend();
    }

    @Override
    public List<String> getPluginSoftDependencies() {
        return this.description.getSoftDepend();
    }

    @Override
    public List<String> getLoadBeforePlugins() {
        return this.description.getLoadBefore();
    }

    @Override
    public List<String> getProvidedPlugins() {
        return this.description.getProvides();
    }

    @Override
    public List<String> getAuthors() {
        return this.description.getAuthors();
    }

    @Override
    public List<String> getContributors() {
        return this.description.getContributors();
    }

    @Override
    public String getDescription() {
        return this.description.getDescription();
    }

    @Override
    public String getWebsite() {
        return this.description.getWebsite();
    }

    @Override
    public List<Permission> getPermissions() {
        return this.description.getPermissions();
    }

    @Override
    public PermissionDefault getPermissionDefault() {
        return this.description.getPermissionDefault();
    }

    @Override
    public String getAPIVersion() {
        return this.description.getAPIVersion();
    }
}
