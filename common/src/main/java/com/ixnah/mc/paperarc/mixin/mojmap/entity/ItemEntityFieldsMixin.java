package com.ixnah.mc.paperarc.mixin.mojmap.entity;

import com.ixnah.mc.paperarc.bridge.ItemEntityBridge;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * Injects Paper's {@code ItemEntity.canMobPickup} supplementary field
 * (Item-canEntityPickup.patch). Field name matches Paper exactly (no
 * {@code paperarc$} prefix) for reflection ABI compatibility; access methods
 * carry the {@code paper$} prefix through {@link ItemEntityBridge} because
 * Paper's patch adds no NMS accessor.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityFieldsMixin implements ItemEntityBridge {

    @Unique
    public boolean canMobPickup = true; // Paper

    @Override
    public boolean paper$canMobPickup() {
        return this.canMobPickup;
    }

    @Override
    public void paper$setCanMobPickup(boolean canMobPickup) {
        this.canMobPickup = canMobPickup;
    }
}
