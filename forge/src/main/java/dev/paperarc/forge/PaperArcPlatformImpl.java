package dev.paperarc.forge;

import dev.paperarc.PaperArcPlatform;

/**
 * Forge 平台侧 {@link PaperArcPlatform} 的实现。
 */
public final class PaperArcPlatformImpl {

    public static String platform() {
        return "forge";
    }

    private PaperArcPlatformImpl() {
    }
}
