package com.ixnah.mc.paperarc.neoforge;

import com.ixnah.mc.paperarc.PaperArcMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(PaperArcNeoForge.MOD_ID)
public final class PaperArcNeoForge {
    public static final String MOD_ID = PaperArcMod.MOD_ID;

    public PaperArcNeoForge(IEventBus modEventBus) {
        // Pre-load bukkit event types referenced by mixin handler descriptors.
        // Under NeoForge's module layer, CraftHumanEntity can be transformed
        // before InventoryCloseEvent$Reason is resolvable, which would fail the
        // mixin apply with ClassMetadataNotFoundException. Forcing an early
        // load pins the class into the shared symbol table first.
        try {
            Class.forName("org.bukkit.event.inventory.InventoryCloseEvent$Reason");
        } catch (Throwable ignored) {
            // bukkit not wired yet: mixins targeting it will retry lazily
        }
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(PaperArcMod::init);
    }
}
