package com.ixnah.mc.paperarc.mixin.common.api;

import com.ixnah.mc.paperarc.bridge.TadpoleBridge;
import net.minecraft.world.entity.animal.frog.Tadpole;
import org.bukkit.craftbukkit.v.entity.CraftTadpole;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Adds Paper's Tadpole age-lock API.
 *
 * <p>Paper stores the lock in a new public {@code Tadpole.ageLocked} field
 * added by its server patches; the field is injected into the NMS class by
 * {@code TadpoleFieldsMixin} and reached here through {@link TadpoleBridge}.
 * Paper adds the field with no accessor methods, so the bridge methods carry
 * the {@code paper$} prefix.
 */
@Mixin(CraftTadpole.class)
public abstract class CraftTadpoleApiMixin {

    @Shadow
    public abstract Tadpole getHandle();

    @Unique
    public boolean getAgeLock() {
        return ((TadpoleBridge) this.getHandle()).paper$getAgeLocked();
    }

    @Unique
    public void setAgeLock(boolean lock) {
        ((TadpoleBridge) this.getHandle()).paper$setAgeLocked(lock);
    }
}
