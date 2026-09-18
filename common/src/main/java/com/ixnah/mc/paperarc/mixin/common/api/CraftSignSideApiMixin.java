package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;
import com.ixnah.mc.paperarc.bridge.api.PaperarcSignLines;
import net.kyori.adventure.text.Component;
import org.bukkit.craftbukkit.v.block.sign.CraftSignSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

/**
 * Port of Paper's Adventure additions on {@link CraftSignSide}:
 * {@code lines()}, {@code line(int)} and {@code line(int, Component)}.
 *
 * <p>PaperAdventure is unavailable here, so vanilla ↔ adventure conversion uses
 * the gson round-trip（{@code bridge.api.PaperarcComponents}）。</p>
 *
 * <p>三个方法都不在 Craft 包装对象上留状态：{@code CraftSignSide} 随
 * {@code CraftSign} 快照每次新建（javap：{@code CraftBlockStates.getBlockState}
 * 方法体逐次 new），缓存一份 {@code List<Component>} 会让读到旧值、
 * 对返回列表的 {@code set} 也永远写不回去（checklist §1.10 an）。
 * {@code lines()} 返回 {@code PaperarcSignLines} —— 直读直写 NMS
 * {@code SignText} 的活动视图。</p>
 */
@Mixin(CraftSignSide.class)
public abstract class CraftSignSideApiMixin {

    @Unique
    public List<Component> lines() {
        return new PaperarcSignLines((CraftSignSide) (Object) this);
    }

    @Unique
    public Component line(int index) {
        return this.lines().get(index);
    }

    @Unique
    public void line(int index, Component line) {
        Preconditions.checkArgument(line != null, "Line cannot be null");
        this.lines().set(index, line); // throws IndexOutOfBoundsException like Paper
    }
}
