package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;

import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftChest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's More-Lidded-Block-API {@code isOpen()} to {@link CraftChest}.
 *
 * <p>Vanilla {@code ChestBlockEntity.openersCounter} (f_155324_) is widened via AT;
 * Paper's patch reads {@code getTileEntity().openersCounter.opened}; vanilla exposes
 * {@code openersCounter.getOpenerCount() > 0} as the closest equivalent.</p>
 */
@Mixin(CraftChest.class)
public abstract class CraftChestApiMixin {

    @Unique
    public boolean isOpen() {
        Object snapshot = ((CraftBlockEntityStateBridge) (Object) this).paperarc$getSnapshot();
        return snapshot instanceof ChestBlockEntity chest && chest.openersCounter.getOpenerCount() > 0;
    }
}
