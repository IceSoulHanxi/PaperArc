package com.ixnah.mc.paperarc.bridge;

/**
 * {@code Beacon#setEffectRange} 真正生效所需的上下文（gaps.md E7）。
 *
 * <p>vanilla 的 {@code BeaconBlockEntity#applyEffects} 是静态方法、形参里没有方块实体，
 * 作用半径写死成 {@code levels * 10 + 10}；Paper 的做法是给它加一个
 * {@code BeaconBlockEntity} 形参。我们改不了签名，于是在唯一调用方 {@code tick} 里把
 * 这台信标的自定义半径压进 ThreadLocal，{@code applyEffects} 里用
 * {@code @ModifyVariable} 读回。没设自定义半径（-1）时不压值，行为与 vanilla 完全一致。
 */
public final class BeaconEffectRangeState {

    private static final ThreadLocal<Double> RANGE = new ThreadLocal<>();

    private BeaconEffectRangeState() {
    }

    public static void push(double range) {
        RANGE.set(range);
    }

    public static Double peek() {
        return RANGE.get();
    }

    public static void pop() {
        RANGE.remove();
    }
}
