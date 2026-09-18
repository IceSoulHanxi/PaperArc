package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.network.chat.Component.Serializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 {@code Objective#displayName()} / {@code displayName(Component)}
 * （A5-3，pairing 基线 NO_IMPL；运行时只有 String 版）。
 *
 * <p>{@code CraftObjective} 是包私有 final 类，只能用 {@code targets=} 字符串；
 * {@code getHandle()} 同样包私有，改走公开的 {@code getDisplayName}/{@code setDisplayName}
 * 与 legacy 串的往返（与本仓库其它 Component ↔ 运行时 String 的做法一致）。</p>
 */
@Mixin(targets = "org.bukkit.craftbukkit.v.scoreboard.CraftObjective", remap = false)
public abstract class CraftObjectiveApiMixin {

    @Unique
    public Component displayName() {
        String legacy = ((org.bukkit.scoreboard.Objective) (Object) this).getDisplayName();
        return legacy == null ? Component.empty()
                : net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                        .legacySection().deserialize(legacy);
    }

    @Unique
    public void displayName(Component displayName) {
        ((org.bukkit.scoreboard.Objective) (Object) this).setDisplayName(displayName == null ? ""
                : net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                        .legacySection().serialize(displayName));
    }
}
