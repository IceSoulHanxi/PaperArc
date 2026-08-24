package com.ixnah.mc.paperarc.forge;

import com.ixnah.mc.paperarc.PaperArcPlatform;

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
