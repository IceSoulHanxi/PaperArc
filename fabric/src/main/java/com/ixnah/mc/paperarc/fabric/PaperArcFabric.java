package com.ixnah.mc.paperarc.fabric;

import com.ixnah.mc.paperarc.PaperArcMod;
import net.fabricmc.api.ModInitializer;

public final class PaperArcFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        PaperArcMod.init();
    }
}
