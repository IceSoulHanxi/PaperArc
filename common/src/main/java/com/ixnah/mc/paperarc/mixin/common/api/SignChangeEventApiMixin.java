package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.block.SignChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

/**
 * paper 的 {@code SignChangeEvent} Component 版行访问（A6/X-2 第六批）。
 * 底层就是事件自己那份 String 数组 —— 改 Component 会真的改掉最终写到牌子上的文本。
 */
@Mixin(SignChangeEvent.class)
public abstract class SignChangeEventApiMixin {

    @Unique
    private SignChangeEvent paperarc$self() {
        return (SignChangeEvent) (Object) this;
    }

    @Unique
    public List<Component> lines() {
        String[] legacy = this.paperarc$self().getLines();
        List<Component> lines = new ArrayList<>(legacy.length);
        for (String line : legacy) {
            lines.add(line == null ? null
                    : LegacyComponentSerializer.legacySection().deserialize(line));
        }
        return lines;
    }

    @Unique
    public Component line(int index) {
        String legacy = this.paperarc$self().getLine(index);
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public void line(int index, Component line) {
        this.paperarc$self().setLine(index, line == null ? null
                : LegacyComponentSerializer.legacySection().serialize(line));
    }
}
