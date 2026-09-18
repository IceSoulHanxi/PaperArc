package com.ixnah.mc.paperarc.bridge.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.block.SignChangeEvent;

import java.util.AbstractList;

/**
 * {@code SignChangeEvent#lines()} 的返回值：直读直写事件的 String 行，
 * 不在事件对象上另存一份（与 {@link PaperarcSignLines} 同理）。
 *
 * <p>类放在 {@code bridge} 包而不是 mixin 包：mixin 包内的类不能被目标类引用。</p>
 */
public final class PaperarcSignEventLines extends AbstractList<Component> {

    private final SignChangeEvent event;

    public PaperarcSignEventLines(SignChangeEvent event) {
        this.event = event;
    }

    @Override
    public Component get(int index) {
        String legacy = this.event.getLine(index);
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Override
    public Component set(int index, Component element) {
        Component old = get(index);
        this.event.setLine(index,
                element == null ? null : LegacyComponentSerializer.legacySection().serialize(element));
        return old;
    }

    @Override
    public int size() {
        return this.event.getLines().length;
    }
}
