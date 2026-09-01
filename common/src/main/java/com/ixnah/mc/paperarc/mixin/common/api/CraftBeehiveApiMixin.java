package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.BeehiveBlockEntityBridge;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import org.bukkit.craftbukkit.v.block.CraftBeehive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Add-EntityBlockStorage-clearEntities {@code clearEntities()} to
 * {@link CraftBeehive}. Paper: {@code getSnapshot().clearBees()} where
 * {@code clearBees()} is a Paper-injected NMS method; here it is reached through
 * {@link BeehiveBlockEntityBridge} merged by {@code BeehiveBlockEntityFieldsMixin}.
 */
@Mixin(CraftBeehive.class)
public abstract class CraftBeehiveApiMixin {

    @Unique
    public void clearEntities() {
        Object snapshot = ((CraftBlockEntityStateBridge) (Object) this).paperarc$getSnapshot();
        if (snapshot instanceof BeehiveBlockEntityBridge bridge) {
            bridge.paper$clearEntities();
        }
    }
}
