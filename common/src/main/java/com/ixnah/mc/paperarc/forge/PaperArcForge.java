package com.ixnah.mc.paperarc.forge;

import com.ixnah.mc.paperarc.PaperArcMod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PaperArcForge.MOD_ID)
public final class PaperArcForge {
    public static final String MOD_ID = PaperArcMod.MOD_ID;

    public PaperArcForge() {
        // 第二阶段注入：超类型链会拉入 org.bukkit 运行时类的类型推迟到这里 define
        // ——此时所有 mixin config 已 prepare 完毕，且早于 Arclight bukkit 层初始化
        // （见 bridge/RuntimeClassInjector 的时序说明）
        com.ixnah.mc.paperarc.bridge.RuntimeClassInjector.defineDeferred();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        // 服务器起来之后把"只有玩家在线才会加载"的注入目标拉一遍，
        // 让 countInjections 覆盖到它们（checklist bi）
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(this::serverStarted);
    }

    private void serverStarted(net.minecraftforge.event.server.ServerStartedEvent event) {
        com.ixnah.mc.paperarc.PaperArcPlatform.logger().info(
                "[PaperArc] injection coverage: "
                        + com.ixnah.mc.paperarc.bridge.PaperarcInjectionCoverage.loadAll());
    }

    private void commonSetup(net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        event.enqueueWork(PaperArcMod::init);
    }
}
