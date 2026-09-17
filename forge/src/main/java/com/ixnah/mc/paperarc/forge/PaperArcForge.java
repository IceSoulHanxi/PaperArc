package com.ixnah.mc.paperarc.forge;

import com.ixnah.mc.paperarc.PaperArcMod;
import com.ixnah.mc.paperarc.bridge.RuntimeClassInjector;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PaperArcForge.MOD_ID)
public final class PaperArcForge {
    public static final String MOD_ID = PaperArcMod.MOD_ID;

    public PaperArcForge() {
        // RuntimeClassInjector 第二阶段：超类型链含 org.bukkit 运行时类的注入类型必须推迟到
        // 这里 define —— mixin config 的 onLoad 阶段同 phase 的其它 config 还在 prepare，
        // 提前 define 会把目标接口加载进来 → MixinTargetAlreadyLoadedException。
        // 模块构造器既晚于所有 config prepare，又早于 Arclight bukkit 层初始化，是唯一合适的时机。
        RuntimeClassInjector.defineDeferred();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        event.enqueueWork(PaperArcMod::init);
    }
}
