package com.ixnah.mc.paperarc.bridge;

import com.destroystokyo.paper.SkinParts;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * 1.20.1 paper-api 的 SKIN_PARTS 选项类型为接口 {@link com.destroystokyo.paper.SkinParts}，
 * 服务端需要一个实现类。位布局参照 Paper 1.20.1 Implement-Player-Client-Options-API 补丁的 PaperSkinParts：
 * cape=1, jacket=2, leftSleeve=4, rightSleeve=8, leftPants=16, rightPants=32, hats=64。
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaperArcSkinParts that = (PaperArcSkinParts) o;
        return raw == that.raw;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(raw);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PaperArcSkinParts.class.getSimpleName() + "[", "]")
            .add("raw=" + raw)
            .add("cape=" + hasCapeEnabled())
            .add("jacket=" + hasJacketEnabled())
            .add("leftSleeve=" + hasLeftSleeveEnabled())
            .add("rightSleeve=" + hasRightSleeveEnabled())
            .add("leftPants=" + hasLeftPantsEnabled())
            .add("rightPants=" + hasRightPantsEnabled())
            .add("hats=" + hasHatsEnabled())
            .toString();
    }
}