package com.ixnah.mc.paperarc.bridge;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorldBorder;
import com.ixnah.mc.paperarc.bridge.PaperArcBridge;
import net.minecraft.world.level.border.WorldBorder;

public final class WorldBorderSupport {

    private WorldBorderSupport() {
    }

    public static World findWorld(WorldBorder handle) {
        for (World world : PaperArcBridge.getServer().getWorlds()) {
            if (((CraftWorldBorder) world.getWorldBorder()).getHandle() == handle) {
                return world;
            }
        }
        return null;
    }
}
