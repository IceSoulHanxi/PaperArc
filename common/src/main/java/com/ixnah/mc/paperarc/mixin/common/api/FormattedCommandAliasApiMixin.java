package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.command.FormattedCommandAlias;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * {@code FormattedCommandAlias} 重写了 paper 的 {@code getTimingName()}（A6/X-2 第五批）：
 * 别名命令在 timings 里要能和真命令区分开。
 */
@Mixin(FormattedCommandAlias.class)
public abstract class FormattedCommandAliasApiMixin {

    @Unique
    public String getTimingName() {
        return "Command Alias: " + ((FormattedCommandAlias) (Object) this).getName();
    }
}
