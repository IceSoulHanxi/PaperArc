package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.TadpoleBridge;
import net.minecraft.world.entity.animal.frog.Tadpole;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code Tadpole.ageLocked} supplementary field
 * (Missing-Entity-API.patch). Field name matches Paper exactly (no
 * {@code paperarc$} prefix) so reflection on the NMS class is ABI-compatible
 * with Paper. Paper adds the field with no accessor methods, so both bridge
 * directions carry the {@code paper$} prefix.
 */
@Mixin(Tadpole.class)
public abstract class TadpoleFieldsMixin implements TadpoleBridge {

    @Unique
    public boolean ageLocked; // Paper

    @Override
    public boolean paper$getAgeLocked() {
        return this.ageLocked;
    }

    @Override
    public void paper$setAgeLocked(boolean ageLocked) {
        this.ageLocked = ageLocked;
    }
}
