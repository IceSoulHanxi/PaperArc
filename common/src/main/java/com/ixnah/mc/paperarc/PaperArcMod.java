package com.ixnah.mc.paperarc;

/**
 * PaperArc 公共入口。
 *
 * <p>paper-api 的实现主体（io.papermc.paper.* / com.destroystokyo.paper.* 的
 * API 类与事件桥接）应放在 common 模块中，通过 mixin 挂接到 Arclight 提供的
 * Bukkit/Spigot 实现之上；各加载器差异部分放在对应平台子模块。</p>
 */
public final class PaperArcMod {
    public static final String MOD_ID = "paperarc";

    private static boolean initialized = false;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        // TODO: 在此挂载 paper-api 事件注册、调度器适配等公共初始化逻辑
        PaperArcPlatform.logger().info("[PaperArc] initialized on platform: " + PaperArcPlatform.platform());
    }

    private PaperArcMod() {
    }
}
