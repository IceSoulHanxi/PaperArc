package dev.paperarc.mixin.common.api;

import com.google.common.base.Preconditions;

import net.minecraft.world.level.block.entity.HopperBlockEntity;

import org.bukkit.block.data.type.Hopper;
import org.bukkit.craftbukkit.v.block.CraftHopper;
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

    @Shadow
    protected abstract HopperBlockEntity getSnapshot();

    @Shadow
    public abstract org.bukkit.block.data.BlockData getBlockData();

    @Shadow
    public abstract void setBlockData(org.bukkit.block.data.BlockData blockData);

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

    @Unique
    public boolean isEnabled() {
        return ((Hopper) this.getBlockData()).isEnabled();
    }

    @Unique
    public void setEnabled(final boolean enabled) {
        Hopper blockData = (Hopper) this.getBlockData();
        blockData.setEnabled(enabled);
        this.setBlockData(blockData);
    }
    // Paper end - Expanded Hopper API
}
