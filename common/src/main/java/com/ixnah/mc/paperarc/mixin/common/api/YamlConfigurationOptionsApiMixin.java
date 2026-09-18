package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.configuration.file.YamlConfigurationOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code YamlConfigurationOptions#codePointLimit}（A6/X-2 第五批）。
 *
 * <p>这是个纯选项持有者，往返读写就是它的全部契约；**真正吃这个值的是
 * {@code YamlConfiguration.loadFromString} 里的 snakeyaml LoaderOptions**，
 * 运行时（spigot 的 YamlConfiguration）没有那一行，所以设了不会真的放宽长度上限。
 * 该语义差异记在 docs/gaps.md —— 不补的话插件调用直接 NoSuchMethodError，更糟。
 */
@Mixin(YamlConfigurationOptions.class)
public abstract class YamlConfigurationOptionsApiMixin {

    @Unique
    private int paperarc$codePointLimit = Integer.MAX_VALUE;

    @Unique
    public int codePointLimit() {
        return this.paperarc$codePointLimit;
    }

    @Unique
    public YamlConfigurationOptions codePointLimit(int codePointLimit) {
        com.google.common.base.Preconditions.checkArgument(codePointLimit > 0,
                "codePointLimit must be positive");
        this.paperarc$codePointLimit = codePointLimit;
        return (YamlConfigurationOptions) (Object) this;
    }
}
