package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;

import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftEnderChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's More-Lidded-Block-API {@code isOpen()} to {@link CraftEnderChest}.
 *
 * <p>Vanilla {@code EnderChestBlockEntity.openersCounter} (f_155511_) is widened via AT;
 * Paper's patch reads {@code getTileEntity().openersCounter.opened}; vanilla exposes
 * {@code openersCounter.getOpenerCount() > 0} as the closest equivalent.</p>
 */
@Mixin(CraftEnderChest.class)
public abstract class CraftEnderChestApiMixin {

    @Unique
    public boolean isOpen() {
        Object snapshot = ((CraftBlockEntityStateBridge) (Object) this).paperarc$getSnapshot();
        return snapshot instanceof EnderChestBlockEntity chest && chest.openersCounter.getOpenerCount() > 0;
    }
}
