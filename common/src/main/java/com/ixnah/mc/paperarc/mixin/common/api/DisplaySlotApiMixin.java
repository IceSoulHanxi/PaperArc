package com.ixnah.mc.paperarc.mixin.common.api;

import org.bukkit.scoreboard.DisplaySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Locale;

/**
 * paper 的 {@code DisplaySlot#getId()}（A6/X-2 第五批）。取值与 paper 一致：
 * BELOW_NAME=belowName、PLAYER_LIST=list、SIDEBAR=sidebar，
 * SIDEBAR_TEAM_* = {@code "sidebar.team." + 小写颜色名}（paper 里就是
 * {@code "sidebar.team." + color}，color 是 NamedTextColor）。
 *
 * <p>paper 还重写了 {@code toString()} 返回 id —— 枚举本来就有 toString，
 * {@code @Unique} 会被丢弃，故不补，记在 docs/gaps.md。
 */
@Mixin(DisplaySlot.class)
public abstract class DisplaySlotApiMixin {

    @Unique
    public String getId() {
        String name = ((DisplaySlot) (Object) this).name();
        if ("BELOW_NAME".equals(name)) {
            return "belowName";
        }
        if ("PLAYER_LIST".equals(name)) {
            return "list";
        }
        if ("SIDEBAR".equals(name)) {
            return "sidebar";
        }
        if (name.startsWith("SIDEBAR_TEAM_")) {
            return "sidebar.team."
                    + name.substring("SIDEBAR_TEAM_".length()).toLowerCase(Locale.ENGLISH);
        }
        return name.toLowerCase(Locale.ENGLISH);
    }
}
