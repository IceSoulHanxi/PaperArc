package com.ixnah.mc.paperarc.mixin.common.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.block.SignChangeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * paper 的 adventure 版 {@code lines()/line(int)/line(int, Component)}
 * （checklist §1.10 am，第 ④ 批）。
 *
 * <p>{@code lines()} 返回的列表对 {@code set} 会写回事件（{@code setLine}），
 * 与 X-1 里 {@code CraftSignSide.lines()} 的做法一致，不在事件对象上缓存。</p>
 */
@Mixin(SignChangeEvent.class)
public abstract class SignChangeEventApiMixin {

    @Unique
    private SignChangeEvent paperarc$self() {
        return (SignChangeEvent) (Object) this;
    }

    @Unique
    public Component line(int index) {
        String legacy = this.paperarc$self().getLine(index);
        return legacy == null ? null : LegacyComponentSerializer.legacySection().deserialize(legacy);
    }

    @Unique
    public void line(int index, Component line) {
        this.paperarc$self().setLine(index,
                line == null ? null : LegacyComponentSerializer.legacySection().serialize(line));
    }

    @Unique
    public java.util.List<Component> lines() {
        SignChangeEvent self = this.paperarc$self();
        return new com.ixnah.mc.paperarc.bridge.api.PaperarcSignEventLines(self);
    }
}
