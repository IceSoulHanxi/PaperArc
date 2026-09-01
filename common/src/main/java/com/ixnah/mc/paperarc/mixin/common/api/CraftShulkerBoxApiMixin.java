package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.craft.CraftBlockEntityStateBridge;

import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.bukkit.craftbukkit.v.block.CraftShulkerBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's More-Lidded-Block-API {@code isOpen()} to {@link CraftShulkerBox}.
 *
 * <p>Vanilla {@code ShulkerBoxBlockEntity.openCount} (f_59646_) is widened via AT;
 * Paper's patch reads {@code getTileEntity().opened} (Paper-injected boolean); vanilla
 * exposes {@code openCount > 0} as the closest equivalent.</p>
 */
@Mixin(CraftShulkerBox.class)
public abstract class CraftShulkerBoxApiMixin {

    @Unique
    public boolean isOpen() {
        Object snapshot = ((CraftBlockEntityStateBridge) (Object) this).paperarc$getSnapshot();
        return snapshot instanceof ShulkerBoxBlockEntity shulker && shulker.openCount > 0;
    }
}
