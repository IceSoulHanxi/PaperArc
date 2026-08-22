package dev.paperarc.fabric;

/**
 * Fabric 平台侧 {@link dev.paperarc.PaperArcPlatform} 的实现。
 */
public final class PaperArcPlatformImpl {

    public static String platform() {
        return "fabric";
    }

    private PaperArcPlatformImpl() {
    }
}
