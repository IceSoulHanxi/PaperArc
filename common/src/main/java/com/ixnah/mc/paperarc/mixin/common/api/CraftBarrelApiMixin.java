package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;

import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftBarrel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's More-Lidded-Block-API {@code isOpen()} to {@link CraftBarrel}.
 *
 * <p>Vanilla {@code BarrelBlockEntity.openersCounter} (f_155050_) is widened via AT;
 * Paper's patch reads {@code getTileEntity().openersCounter.opened}, and vanilla has
 * {@code openersCounter.getOpenerCount() > 0} as the closest equivalent.</p>
 */
@Mixin(CraftBarrel.class)
public abstract class CraftBarrelApiMixin {

    @Unique
    public boolean isOpen() {
        Object snapshot = ((CraftBlockEntityStateBridge) (Object) this).paperarc$getSnapshot();
        return snapshot instanceof BarrelBlockEntity barrel && barrel.openersCounter.getOpenerCount() > 0;
    }
}
