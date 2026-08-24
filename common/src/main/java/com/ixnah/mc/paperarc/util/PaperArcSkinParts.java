package com.ixnah.mc.paperarc.util;

import com.destroystokyo.paper.SkinParts;

/**
 * SkinParts 的最小实现（对应 Paper 服务端内部类 com.destroystokyo.paper.PaperSkinParts，
 * 该类不在 paper-api 中，PaperArc 无法引用，故自带等价实现）。
 * 位序与 Paper 完全一致：bit0 cape、bit1 jacket、bit2 leftSleeve、bit3 rightSleeve、
 * bit4 leftPants、bit5 rightPants、bit6 hat。
 */
public class PaperArcSkinParts implements SkinParts {

    private final int raw;

    public PaperArcSkinParts(int raw) {
        this.raw = raw;
    }

    @Override
    public boolean hasCapeEnabled() {
        return (raw & 1) == 1;
    }

    @Override
    public boolean hasJacketEnabled() {
        return (raw >> 1 & 1) == 1;
    }

    @Override
    public boolean hasLeftSleeveEnabled() {
        return (raw >> 2 & 1) == 1;
    }

    @Override
    public boolean hasRightSleeveEnabled() {
        return (raw >> 3 & 1) == 1;
    }

    @Override
    public boolean hasLeftPantsEnabled() {
        return (raw >> 4 & 1) == 1;
    }

    @Override
    public boolean hasRightPantsEnabled() {
        return (raw >> 5 & 1) == 1;
    }

    @Override
    public boolean hasHatsEnabled() {
        return (raw >> 6 & 1) == 1;
    }

    @Override
    public int getRaw() {
        return raw;
    }
}
