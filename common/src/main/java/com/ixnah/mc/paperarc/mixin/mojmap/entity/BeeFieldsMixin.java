package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.BeeBridge;
import net.kyori.adventure.util.TriState;
import net.minecraft.world.entity.animal.Bee;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code Bee.rollingOverride} supplementary field
 * (Missing-Entity-API.patch). Field name matches Paper exactly (no
 * {@code paperarc$} prefix) for reflection ABI compatibility; access methods
 * carry the {@code paper$} prefix through {@link BeeBridge} because Paper's
 * patch adds no NMS accessor.
 */
@Mixin(Bee.class)
public abstract class BeeFieldsMixin implements BeeBridge {

    @Unique
    public TriState rollingOverride = TriState.NOT_SET; // Paper

    @Override
    public TriState paper$getRollingOverride() {
        return this.rollingOverride;
    }

    @Override
    public void paper$setRollingOverride(TriState rolling) {
        this.rollingOverride = rolling == null ? TriState.NOT_SET : rolling;
    }
}
