package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.FallingBlockEntityBridge;
import net.minecraft.world.entity.item.FallingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code FallingBlockEntity.autoExpire} supplementary field
 * (FallingBlock-auto-expire-setting.patch). Field name matches Paper exactly (no
 * {@code paperarc$} prefix) for reflection ABI compatibility; access methods
 * carry the {@code paper$} prefix through {@link FallingBlockEntityBridge}
 * because Paper's patch adds no NMS accessor.
 */
@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityFieldsMixin implements FallingBlockEntityBridge {

    @Unique
    public boolean autoExpire = true; // Paper

    @Override
    public boolean paper$autoExpire() {
        return this.autoExpire;
    }

    @Override
    public void paper$setAutoExpire(boolean autoExpire) {
        this.autoExpire = autoExpire;
    }
}
