package com.ixnah.mc.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;

import net.minecraft.world.level.block.entity.HopperBlockEntity;

import org.bukkit.craftbukkit.v.block.CraftHopper;
import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Port of Paper's Expanded-Hopper-API.patch additions on
 * {@link CraftHopper}: transfer cooldown accessors and the
 * redstone-driven enabled flag.
 */
@Mixin(CraftHopper.class)
public abstract class CraftHopperApiMixin {

    @Unique
    private HopperBlockEntity getSnapshot() {
        return (HopperBlockEntity) ((CraftBlockEntityStateBridge) (Object) this).paperarc$getSnapshot();
    }

    // Paper start - Expanded Hopper API
    @Unique
    public int getTransferCooldown() {
        return ((HopperBlockEntityInvokerMixin) (Object) this.getSnapshot()).paperarc$cooldownTime();
    }

    @Unique
    public void setTransferCooldown(final int cooldown) {
        Preconditions.checkArgument(cooldown >= 0, "Hooper transfer cooldown cannot be negative (" + cooldown + ")");
        ((HopperBlockEntityInvokerMixin) (Object) this.getSnapshot()).paperarc$invokeSetCooldown(cooldown);
    }
    // Paper end - Expanded Hopper API
}
