package com.ixnah.mc.paperarc.bridge.api;

/**
 * paper 的方块坐标打包/解包（{@code Block.getBlockKey*}），位布局逐位照抄 paper-api。
 *
 * <p>为什么不直接调 {@code Block.getBlockKeyX(…)}：那几个是 paper 加在
 * {@code org.bukkit.block.Block} 接口上的 <b>static</b> 方法，运行时的 Block 接口没有，
 * 调了就是 {@code NoSuchMethodError}（checklist §1.6 n 那一类）。静态方法也补不进接口 ——
 * Mixin 只合并实例成员。所以引用方一律走这里。
 */
public final class PaperarcBlockKeys {

    private PaperarcBlockKeys() {
    }

    public static long pack(int x, int y, int z) {
        return (long) x & 134217727L | ((long) z & 134217727L) << 27 | (long) y << 54;
    }

    public static int unpackX(long packed) {
        return (int) (packed << 37 >> 37);
    }

    public static int unpackY(long packed) {
        return (int) (packed >> 54);
    }

    public static int unpackZ(long packed) {
        return (int) (packed << 10 >> 37);
    }

    /** paper 的 {@code Chunk.getChunkKey(int, int)}，同样是接口 static 方法。 */
    public static long packChunk(int x, int z) {
        return (long) x & 4294967295L | ((long) z & 4294967295L) << 32;
    }
}
