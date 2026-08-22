package dev.paperarc.fabric;

import dev.paperarc.PaperArcMod;
import net.fabricmc.api.ModInitializer;

public final class PaperArcFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        PaperArcMod.init();
    }
}
