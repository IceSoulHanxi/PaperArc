package com.ixnah.mc.paperarc;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.util.logging.Logger;

/**
 * 平台抽象（Architectury ExpectPlatform 模式）。
 *
 * <p>每个平台子模块需提供同名同签名的静态实现：
 * <ul>
 *   <li>forge   -> {@code com.ixnah.mc.paperarc.forge.PaperArcPlatformImpl}</li>
 *   <li>neoforge-> {@code com.ixnah.mc.paperarc.neoforge.PaperArcPlatformImpl}</li>
 *   <li>fabric  -> {@code com.ixnah.mc.paperarc.fabric.PaperArcPlatformImpl}</li>
 * </ul>
 * Architectury 注解处理器会在编译期生成分发代码，运行时按实际加载器路由。</p>
 */
public final class PaperArcPlatform {

    /**
     * @return 当前加载器平台名：forge / neoforge / fabric
     */
    @ExpectPlatform
    public static String platform() {
        throw new AssertionError("platform() is platform-specific and must be implemented");
    }

    public static Logger logger() {
        return Logger.getLogger(PaperArcMod.MOD_ID);
    }

    private PaperArcPlatform() {
    }
}
