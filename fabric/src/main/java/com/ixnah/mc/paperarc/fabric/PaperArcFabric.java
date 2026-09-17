package com.ixnah.mc.paperarc.fabric;

import com.ixnah.mc.paperarc.PaperArcMod;
import com.ixnah.mc.paperarc.bridge.RuntimeClassInjector;
import net.fabricmc.api.ModInitializer;

public final class PaperArcFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // RuntimeClassInjector 第二阶段，理由见 PaperArcForge 同处注释。
        // Knot 下 phase1 的 transformerLoader hook 不适用（那是 ModLauncher 的接口），
        // 缺失类型改由 fabric 产物直接携带（见 fabric/build.gradle 的 shadowJar 例外），
        // 这里仍调一次：类已存在时 defineAll 会跳过，纯幂等。
        RuntimeClassInjector.defineDeferred();
        PaperArcMod.init();
    }
}
