package dev.paperarc.forge;

import dev.paperarc.PaperArcMod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PaperArcForge.MOD_ID)
public final class PaperArcForge {
    public static final String MOD_ID = PaperArcMod.MOD_ID;

    public PaperArcForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        event.enqueueWork(PaperArcMod::init);
    }
}
