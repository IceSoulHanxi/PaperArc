package dev.paperarc.neoforge;

import dev.paperarc.PaperArcMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(PaperArcNeoForge.MOD_ID)
public final class PaperArcNeoForge {
    public static final String MOD_ID = PaperArcMod.MOD_ID;

    public PaperArcNeoForge(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(PaperArcMod::init);
    }
}
