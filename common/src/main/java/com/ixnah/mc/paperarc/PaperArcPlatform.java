package com.ixnah.mc.paperarc;

import java.util.logging.Logger;

/**
 * 平台抽象（ForgeGradle 单工程：当前仅 Forge，1.20.1 跟随 Arclight Trials Forge-only）。
 */
public final class PaperArcPlatform {

    /**
     * @return 当前加载器平台名
     */
    public static String platform() {
        return "forge";
    }

    public static Logger logger() {
        return Logger.getLogger(PaperArcMod.MOD_ID);
    }

    private PaperArcPlatform() {
    }
}
