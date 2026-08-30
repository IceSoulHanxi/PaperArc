package com.ixnah.mc.paperarc.mixin.mojmap.block;

import com.ixnah.mc.paperarc.bridge.CampfireBlockEntityBridge;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code CampfireBlockEntity.stopCooking} supplementary field
 * (Add-more-Campfire-API.patch). Field name matches Paper exactly (no
 * {@code paperarc$} prefix) for reflection ABI compatibility; access methods
 * carry the {@code paper$} prefix through {@link CampfireBlockEntityBridge}
 * because Paper's patch adds no NMS accessor.
 */
@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityFieldsMixin implements CampfireBlockEntityBridge {

    @Unique
    public final boolean[] stopCooking = new boolean[4]; // Paper

    @Override
    public boolean paper$isStopCooking(int index) {
        return this.stopCooking[index];
    }

    @Override
    public void paper$setStopCooking(int index, boolean stop) {
        this.stopCooking[index] = stop;
    }
}
