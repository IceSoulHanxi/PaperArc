package com.ixnah.mc.paperarc.forge;

import com.ixnah.mc.paperarc.PaperArcMod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PaperArcForge.MOD_ID)
public final class PaperArcForge {
    public static final String MOD_ID = PaperArcMod.MOD_ID;

    public PaperArcForge() {
        // mod 构造期注入运行时缺失的 paper-api 类型（见 bridge/RuntimeClassInjector）
        // （必须在任何 org.bukkit.* 接口被加载/转换之前完成）
        com.ixnah.mc.paperarc.bridge.RuntimeClassInjector.inject();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        event.enqueueWork(PaperArcMod::init);
    }
}
